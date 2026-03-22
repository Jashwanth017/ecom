package com.sample.marketplace.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SellerRegistrationRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password,

        @NotBlank(message = "Store name is required")
        @Size(max = 150, message = "Store name must be at most 150 characters")
        String storeName,

        @Size(max = 1000, message = "Store description must be at most 1000 characters")
        String storeDescription
) {
}
