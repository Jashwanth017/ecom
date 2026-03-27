package com.sample.marketplace.dto.order;

public record OrderDeliveryAddressResponse(
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode
) {
}
