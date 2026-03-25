package com.sample.marketplace.dto.buyer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBuyerProfileRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 120, message = "Full name must be at most 120 characters")
        String fullName,

        @Size(max = 20, message = "Phone must be at most 20 characters")
        String phone
) {
}
