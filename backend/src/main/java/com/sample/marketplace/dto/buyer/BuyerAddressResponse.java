package com.sample.marketplace.dto.buyer;

public record BuyerAddressResponse(
        Long addressId,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode
) {
}
