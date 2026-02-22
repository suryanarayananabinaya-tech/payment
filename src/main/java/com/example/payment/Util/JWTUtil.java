package com.example.payment.Util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;

@Component
public class JWTUtil {


    private final SecretKey secretKey;

    public JWTUtil(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public String generateAccessToken(String userName) {
        return Jwts.builder()
                .setSubject(userName)
                .claim("type", "ACCESS")
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(7, ChronoUnit.DAYS)))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /* ================= TOKEN VALIDATION ================= */

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token); // validates signature + expiry
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isRefreshTokenValid(String token) {
        return isTokenValid(token) &&
                "REFRESH".equals(extractClaim(token, c -> c.get("type", String.class)));
    }

    public boolean isAccessTokenValid(String token) {
        return isTokenValid(token) &&
                "ACCESS".equals(extractClaim(token, c -> c.get("type", String.class)));
    }

    /* ================= CLAIM EXTRACTION ================= */

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
