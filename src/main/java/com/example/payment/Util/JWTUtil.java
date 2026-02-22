package com.example.payment.Util;


import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JWTUtil {


    private final SecretKey secretKey;

    public JWTUtil(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public String generateToken(String userName) {
        return Jwts.builder()
                .setSubject(userName)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(7, ChronoUnit.DAYS)))
                .claim("type", "refresh")
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String jwtToken) {
        try{
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(jwtToken);
            return true;

        }catch (JwtException e){
            return false;
        }
    }


    public String extractUsername(String jwtToken) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(jwtToken).getBody().getSubject();
    }
}
