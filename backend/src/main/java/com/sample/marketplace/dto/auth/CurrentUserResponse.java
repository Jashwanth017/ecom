package com.sample.marketplace.dto.auth;

import com.sample.marketplace.models.enums.Role;
import com.sample.marketplace.models.enums.SellerApprovalStatus;
import com.sample.marketplace.models.enums.UserStatus;

public record CurrentUserResponse(
        Long id,
        String email,
        Role role,
        UserStatus status,
        SellerApprovalStatus sellerApprovalStatus
) {
}
