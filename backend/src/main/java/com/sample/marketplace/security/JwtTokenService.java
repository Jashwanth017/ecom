package com.sample.marketplace.security;

import com.sample.marketplace.models.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final JwtProperties jwtProperties;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(AuthenticatedUser authenticatedUser) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtProperties.expirationMs());

        return Jwts.builder()
                .subject(String.valueOf(authenticatedUser.getId()))
                .claims(Map.of(
                        "email", authenticatedUser.getEmail(),
                        "role", authenticatedUser.getRole().name(),
                        "status", authenticatedUser.getStatus().name(),
                        "type", "access"
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey())
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public String extractEmail(String token) {
        return extractClaims(token).get("email", String.class);
    }

    public Role extractRole(String token) {
        return Role.valueOf(extractClaims(token).get("role", String.class));
    }

    public boolean isAccessToken(String token) {
        return "access".equals(extractClaims(token).get("type", String.class));
    }

    public String extractSubject(String token) {
        return extractClaims(token).getSubject();
    }

    public long accessTokenExpiresIn() {
        return jwtProperties.expirationMs();
    }

    public long refreshTokenExpiresIn() {
        return jwtProperties.refreshExpirationMs();
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Key signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }
}
