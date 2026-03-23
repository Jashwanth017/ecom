package com.sample.marketplace.dto.order;

import com.sample.marketplace.models.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record BuyerOrderSummaryResponse(
        Long orderId,
        OrderStatus status,
        BigDecimal totalAmount,
        Instant placedAt,
        int itemCount
) {
}
