package com.sample.marketplace.dto.cart;

import java.math.BigDecimal;

public record CartItemResponse(
        Long cartItemId,
        Long productId,
        String productName,
        String imageUrl,
        BigDecimal price,
        Integer quantity,
        BigDecimal lineTotal
) {
}
