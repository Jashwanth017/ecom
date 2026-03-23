package com.sample.marketplace.dto.order;

import com.sample.marketplace.models.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record SellerOrderItemResponse(
        Long orderItemId,
        Long orderId,
        Long productId,
        String productName,
        BigDecimal productPrice,
        Integer quantity,
        BigDecimal lineTotal,
        OrderStatus orderStatus,
        Instant placedAt
) {
}
