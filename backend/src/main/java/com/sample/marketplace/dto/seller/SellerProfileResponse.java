package com.sample.marketplace.dto.seller;

import com.sample.marketplace.models.enums.SellerApprovalStatus;

public record SellerProfileResponse(
        Long sellerProfileId,
        Long userId,
        String email,
        String storeName,
        String storeDescription,
        SellerApprovalStatus approvalStatus,
        String rejectionReason
) {
}
