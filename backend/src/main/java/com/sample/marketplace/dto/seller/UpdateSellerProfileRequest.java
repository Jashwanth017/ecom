package com.sample.marketplace.dto.seller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSellerProfileRequest(
        @NotBlank(message = "Store name is required")
        @Size(max = 150, message = "Store name must be at most 150 characters")
        String storeName,

        @Size(max = 1000, message = "Store description must be at most 1000 characters")
        String storeDescription
) {
}
