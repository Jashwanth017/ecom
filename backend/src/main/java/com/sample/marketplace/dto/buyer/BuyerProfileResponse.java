package com.sample.marketplace.dto.buyer;

public record BuyerProfileResponse(
        Long buyerProfileId,
        Long userId,
        String email,
        String fullName,
        String phone,
        java.util.List<BuyerAddressResponse> addresses
) {
}
