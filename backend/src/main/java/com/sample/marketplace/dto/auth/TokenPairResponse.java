package com.sample.marketplace.dto.auth;

public record TokenPairResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        long refreshExpiresIn
) {
}
