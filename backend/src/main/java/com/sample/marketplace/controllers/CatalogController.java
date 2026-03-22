package com.sample.marketplace.controllers;

import com.sample.marketplace.dto.catalog.CatalogProductSummaryResponse;
import com.sample.marketplace.dto.catalog.CategoryResponse;
import com.sample.marketplace.dto.catalog.ProductDetailResponse;
import com.sample.marketplace.services.CatalogService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/products")
    public List<CatalogProductSummaryResponse> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sort
    ) {
        return catalogService.getProducts(search, categoryId, sort);
    }

    @GetMapping("/products/{productId}")
    public ProductDetailResponse getProductDetail(@PathVariable Long productId) {
        return catalogService.getProductDetail(productId);
    }

    @GetMapping("/categories")
    public List<CategoryResponse> getCategories() {
        return catalogService.getCategories();
    }
}
