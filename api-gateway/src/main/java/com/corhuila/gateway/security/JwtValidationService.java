package com.corhuila.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtValidationService {

    private final SecretKey signingKey;

    public JwtValidationService(@Value("${security.jwt.secret:${JWT_SECRET:}}") String secret) {
        if (secret == null || secret.isBlank() || secret.startsWith("change_me")) {
            throw new IllegalStateException("JWT_SECRET must be configured with a non-placeholder value");
        }
        this.signingKey = Keys.hmacShaKeyFor(sha256(secret));
    }

    public JwtClaims validate(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Date expiration = claims.getExpiration();
        if (expiration == null || !expiration.after(new Date())) {
            throw new JwtException("JWT is expired");
        }

        Object userId = claims.get("uid");
        String username = claims.getSubject();
        if (userId == null || username == null || username.isBlank()) {
            throw new JwtException("JWT identity claims are missing");
        }

        return new JwtClaims(
                String.valueOf(userId),
                username,
                optionalString(claims.get("tenantId")),
                optionalString(claims.get("tenantSlug")),
                getStringList(claims, "roles"),
                getStringList(claims, "permissions")
        );
    }

    private String optionalString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private List<String> getStringList(Claims claims, String claimName) {
        Object value = claims.get(claimName);
        if (!(value instanceof List<?> values)) {
            return List.of();
        }
        return values.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList();
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
