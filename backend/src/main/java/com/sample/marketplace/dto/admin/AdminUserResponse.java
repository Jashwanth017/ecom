package com.sample.marketplace.dto.admin;

import com.sample.marketplace.models.enums.Role;
import com.sample.marketplace.models.enums.SellerApprovalStatus;
import com.sample.marketplace.models.enums.UserStatus;
import java.time.Instant;

public record AdminUserResponse(
        Long userId,
        Long sellerProfileId,
        String email,
        Role role,
        UserStatus status,
        SellerApprovalStatus sellerApprovalStatus,
        String storeName,
        Instant createdAt
) {
}
