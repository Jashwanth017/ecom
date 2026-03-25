package com.sample.marketplace.dto.buyer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BuyerAddressRequest(
        @NotBlank(message = "Address line 1 is required")
        @Size(max = 255, message = "Address line 1 must be at most 255 characters")
        String addressLine1,

        @Size(max = 255, message = "Address line 2 must be at most 255 characters")
        String addressLine2,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must be at most 100 characters")
        String city,

        @NotBlank(message = "State is required")
        @Size(max = 100, message = "State must be at most 100 characters")
        String state,

        @NotBlank(message = "Postal code is required")
        @Size(max = 20, message = "Postal code must be at most 20 characters")
        String postalCode
) {
}
