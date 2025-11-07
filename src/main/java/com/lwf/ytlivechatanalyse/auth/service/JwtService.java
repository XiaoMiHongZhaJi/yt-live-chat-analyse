package com.lwf.ytlivechatanalyse.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    private final Key key;
    private final long expiresInSeconds;

    public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expires-in-seconds}") long expiresInSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiresInSeconds = expiresInSeconds;
    }

    public String generateToken(String userId, String userName) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiresInSeconds * 1000);
        return Jwts.builder()
                .claim("userId", userId)
                .claim("userName", userName)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}
