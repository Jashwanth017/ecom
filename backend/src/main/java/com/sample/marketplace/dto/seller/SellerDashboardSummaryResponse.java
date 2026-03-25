package com.sample.marketplace.dto.seller;

import java.math.BigDecimal;

public record SellerDashboardSummaryResponse(
        long totalOrders,
        long totalProducts,
        BigDecimal totalMoneyGenerated
) {
}
