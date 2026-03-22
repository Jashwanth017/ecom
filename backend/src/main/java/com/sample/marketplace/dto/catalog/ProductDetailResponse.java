package com.sample.marketplace.dto.catalog;

import java.math.BigDecimal;

public record ProductDetailResponse(
        Long productId,
        Long categoryId,
        String categoryName,
        String sellerStoreName,
        String name,
        String description,
        BigDecimal price,
        String imageUrl
) {
}
