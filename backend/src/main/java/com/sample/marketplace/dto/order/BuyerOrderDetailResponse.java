package com.sample.marketplace.dto.order;

import com.sample.marketplace.models.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BuyerOrderDetailResponse(
        Long orderId,
        OrderStatus status,
        BigDecimal totalAmount,
        Instant placedAt,
        OrderDeliveryAddressResponse deliveryAddress,
        List<OrderItemResponse> items
) {
}
