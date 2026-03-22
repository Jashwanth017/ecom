package com.sample.marketplace.dto.buyer;

public record BuyerProfileResponse(
        Long buyerProfileId,
        Long userId,
        String email,
        String fullName,
        String phone,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode
) {
}
