package com.sample.marketplace.dto.order;

import jakarta.validation.constraints.NotNull;

public record PlaceOrderRequest(
        @NotNull(message = "Address id is required")
        Long addressId
) {
}
