package com.example.payment.config;

import com.example.payment.dto.RateLimitResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.function.Supplier;

@Component
@ConditionalOnProperty(name = "rate.limit.enabled", havingValue = "true", matchIfMissing = true)
public class PaymentRateLimitFilter extends OncePerRequestFilter {

    private final ProxyManager<String> proxyManager;
    private final BucketConfiguration bucketConfiguration;
    private final ObjectMapper objectMapper;

    public PaymentRateLimitFilter(ProxyManager<String> proxyManager,
                                  BucketConfiguration bucketConfiguration,
                                  ObjectMapper objectMapper) {
        this.proxyManager = proxyManager;
        this.bucketConfiguration = bucketConfiguration;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("/api/v1/payments".equals(request.getRequestURI())
                && HttpMethod.POST.matches(request.getMethod()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String key = buildRateLimitKey(request);
        Supplier<BucketConfiguration> configSupplier = () -> bucketConfiguration;

        Bucket bucket = proxyManager.builder().build(key, configSupplier);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(1, probe.getNanosToWaitForRefill() / 1_000_000_000);

        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

        RateLimitResponseDTO body = new RateLimitResponseDTO(
                "RATE_LIMIT_EXCEEDED",
                "Too many requests. Please try again later."
        );

        response.getWriter().write(objectMapper.writeValueAsString(body));
        return;
    }

    private String buildRateLimitKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = "anonymous";
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getName() != null
                && !"anonymousUser".equals(authentication.getName())) {
            username = authentication.getName();
        }

        String clientIp = extractClientIp(request);
        return "rate-limit:payments:" + username + ":" + clientIp;
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}