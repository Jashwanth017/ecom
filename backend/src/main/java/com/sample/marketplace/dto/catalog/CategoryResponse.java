package com.sample.marketplace.dto.catalog;

public record CategoryResponse(
        Long categoryId,
        String name,
        String slug
) {
}
