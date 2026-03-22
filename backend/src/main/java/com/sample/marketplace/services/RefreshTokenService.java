package com.sample.marketplace.services;

import com.sample.marketplace.exception.InvalidRefreshTokenException;
import com.sample.marketplace.models.RefreshToken;
import com.sample.marketplace.models.User;
import com.sample.marketplace.repositories.RefreshTokenRepository;
import com.sample.marketplace.security.JwtProperties;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            JwtProperties jwtProperties
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
    }

    public RefreshToken createRefreshToken(User user) {
        revokeAllActiveTokens(user);
        Instant expiresAt = Instant.now().plusMillis(jwtProperties.refreshExpirationMs());
        RefreshToken refreshToken = RefreshToken.create(user, UUID.randomUUID().toString(), expiresAt);
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new InvalidRefreshTokenException("Refresh token has been revoked");
        }
        if (refreshToken.isExpired()) {
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }
        return refreshToken;
    }

    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.revoke();
            refreshTokenRepository.save(refreshToken);
        });
    }

    public void revokeAllActiveTokens(User user) {
        refreshTokenRepository.findAllByUserAndRevokedFalse(user).forEach(token -> token.revoke());
    }

    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteAllByExpiresAtBefore(Instant.now());
    }
}
