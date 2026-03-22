package com.sample.marketplace.dto.seller;

import com.sample.marketplace.models.enums.ProductStatus;
import java.math.BigDecimal;

public record SellerProductResponse(
        Long productId,
        Long sellerId,
        Long categoryId,
        String categoryName,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        ProductStatus status,
        String imageUrl
) {
}
