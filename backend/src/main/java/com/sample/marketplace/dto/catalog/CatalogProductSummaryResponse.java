package com.sample.marketplace.dto.catalog;

import java.math.BigDecimal;

public record CatalogProductSummaryResponse(
        Long productId,
        Long categoryId,
        String categoryName,
        String name,
        BigDecimal price,
        String imageUrl
) {
}
