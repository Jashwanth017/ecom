package com.sample.marketplace.dto.cart;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long cartId,
        Long buyerId,
        List<CartItemResponse> items,
        BigDecimal totalAmount
) {
}
