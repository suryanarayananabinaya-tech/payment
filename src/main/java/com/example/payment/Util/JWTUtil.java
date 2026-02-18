package com.example.payment.Util;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JWTUtil {


    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long expiration =  1000*60*60;

    public String generateToken(String userName) {
        return Jwts.builder()
                .setSubject(userName)
                .setExpiration(new Date(System.currentTimeMillis()+ expiration))
                .signWith(secretKey).compact();
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
