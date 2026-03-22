package com.sample.marketplace.dto.seller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateStockRequest(
        @NotNull(message = "Stock quantity is required")
        @PositiveOrZero(message = "Stock quantity must be non-negative")
        Integer stockQuantity
) {
}
