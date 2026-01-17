package com.pairapp.security;

import com.pairapp.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    public String generateToken(UUID userId, String email) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(properties.accessTokenTtlMinutes() * 60);
        return Jwts.builder()
                .subject(userId.toString())
                .issuer(properties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("email", email)
                .signWith(Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public UUID parseUserId(String token) {
        String subject = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return UUID.fromString(subject);
    }
}
