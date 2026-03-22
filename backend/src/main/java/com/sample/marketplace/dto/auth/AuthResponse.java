package com.sample.marketplace.dto.auth;

import com.sample.marketplace.models.enums.Role;
import com.sample.marketplace.models.enums.SellerApprovalStatus;
import com.sample.marketplace.models.enums.UserStatus;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        long refreshExpiresIn,
        Long userId,
        String email,
        Role role,
        UserStatus status,
        SellerApprovalStatus sellerApprovalStatus,
        String redirectTo,
        String message
) {
}
