package com.example.payment.Security;

import com.example.payment.Util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTAuthenticationFilter  extends OncePerRequestFilter {

    private final CustomUserDetailsService customUserDetailsService;

    private final JWTUtil jwtUtil;

    public JWTAuthenticationFilter(JWTUtil jwtUtil, CustomUserDetailsService customUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final  String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {
           String jwtToken = authorization.substring(7);
            // ✅ Only allow ACCESS tokens for request authentication
             if(jwtUtil.isAccessTokenValid( jwtToken)){
                 String username = jwtUtil.extractUsername(jwtToken);
                 UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                 UsernamePasswordAuthenticationToken authToken =
                         new UsernamePasswordAuthenticationToken(
                                 userDetails, null, userDetails.getAuthorities()
                         );
                 authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                 SecurityContextHolder.getContext().setAuthentication(authToken);
             }
        }
        filterChain.doFilter(request,response);

    }



}
