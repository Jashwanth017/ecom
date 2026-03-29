package com.sample.marketplace.controllers;

import com.sample.marketplace.dto.seller.CreateProductRequest;
import com.sample.marketplace.dto.seller.ProductImageUploadResponse;
import com.sample.marketplace.dto.seller.SellerDashboardSummaryResponse;
import com.sample.marketplace.dto.seller.SellerProductResponse;
import com.sample.marketplace.dto.seller.SellerProfileResponse;
import com.sample.marketplace.dto.seller.UpdateProductRequest;
import com.sample.marketplace.dto.seller.UpdateSellerProfileRequest;
import com.sample.marketplace.dto.seller.UpdateStockRequest;
import com.sample.marketplace.dto.catalog.CategoryResponse;
import com.sample.marketplace.security.AuthenticatedUser;
import com.sample.marketplace.services.SellerCatalogService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/seller")
public class SellerController {

    private final SellerCatalogService sellerCatalogService;

    public SellerController(SellerCatalogService sellerCatalogService) {
        this.sellerCatalogService = sellerCatalogService;
    }

    @GetMapping("/dashboard/summary")
    public SellerDashboardSummaryResponse getDashboardSummary(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return sellerCatalogService.getDashboardSummary(authenticatedUser);
    }

    @GetMapping("/profile")
    public SellerProfileResponse getSellerProfile(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return sellerCatalogService.getSellerProfile(authenticatedUser);
    }

    @PutMapping("/profile")
    public SellerProfileResponse updateSellerProfile(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody UpdateSellerProfileRequest request
    ) {
        return sellerCatalogService.updateSellerProfile(authenticatedUser, request);
    }

    @PostMapping("/products")
    public SellerProductResponse createProduct(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateProductRequest request
    ) {
        return sellerCatalogService.createProduct(authenticatedUser, request);
    }

    @PostMapping("/products/image")
    public ProductImageUploadResponse uploadProductImage(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam("image") MultipartFile image
    ) {
        return sellerCatalogService.uploadProductImage(authenticatedUser, image);
    }

    @GetMapping("/products")
    public List<SellerProductResponse> getOwnProducts(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return sellerCatalogService.getOwnProducts(authenticatedUser);
    }

    @GetMapping("/categories/used")
    public List<CategoryResponse> getUsedCategories(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return sellerCatalogService.getUsedCategories(authenticatedUser);
    }

    @GetMapping("/products/{productId}")
    public SellerProductResponse getOwnProduct(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long productId
    ) {
        return sellerCatalogService.getOwnProduct(authenticatedUser, productId);
    }

    @PutMapping("/products/{productId}")
    public SellerProductResponse updateProduct(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return sellerCatalogService.updateProduct(authenticatedUser, productId, request);
    }

    @PatchMapping("/products/{productId}/disable")
    public SellerProductResponse disableProduct(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long productId
    ) {
        return sellerCatalogService.disableProduct(authenticatedUser, productId);
    }

    @PatchMapping("/products/{productId}/enable")
    public SellerProductResponse enableProduct(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long productId
    ) {
        return sellerCatalogService.enableProduct(authenticatedUser, productId);
    }

    @PatchMapping("/products/{productId}/stock")
    public SellerProductResponse updateStock(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateStockRequest request
    ) {
        return sellerCatalogService.updateStock(authenticatedUser, productId, request);
    }
}
