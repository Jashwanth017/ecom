package com.sample.marketplace.dto.admin;

import jakarta.validation.constraints.Size;

public record SellerApprovalActionRequest(
        @Size(max = 500, message = "Reason must be at most 500 characters")
        String reason
) {
}
