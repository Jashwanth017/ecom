package com.sample.marketplace.dto.order;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long orderItemId,
        Long productId,
        String productName,
        BigDecimal productPrice,
        Integer quantity,
        BigDecimal lineTotal
) {
}
