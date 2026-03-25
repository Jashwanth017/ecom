package com.sample.marketplace.services;

import com.sample.marketplace.dto.seller.CreateProductRequest;
import com.sample.marketplace.dto.seller.SellerDashboardSummaryResponse;
import com.sample.marketplace.dto.seller.SellerProductResponse;
import com.sample.marketplace.dto.seller.SellerProfileResponse;
import com.sample.marketplace.dto.seller.UpdateProductRequest;
import com.sample.marketplace.dto.seller.UpdateSellerProfileRequest;
import com.sample.marketplace.dto.seller.UpdateStockRequest;
import com.sample.marketplace.dto.catalog.CategoryResponse;
import com.sample.marketplace.models.Category;
import com.sample.marketplace.models.Product;
import com.sample.marketplace.models.SellerProfile;
import com.sample.marketplace.models.enums.ProductStatus;
import com.sample.marketplace.models.enums.Role;
import com.sample.marketplace.models.enums.SellerApprovalStatus;
import com.sample.marketplace.repositories.CategoryRepository;
import com.sample.marketplace.repositories.OrderItemRepository;
import com.sample.marketplace.repositories.ProductRepository;
import com.sample.marketplace.repositories.SellerProfileRepository;
import com.sample.marketplace.security.AuthenticatedUser;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SellerCatalogService {

    private final SellerProfileRepository sellerProfileRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    public SellerCatalogService(
            SellerProfileRepository sellerProfileRepository,
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.sellerProfileRepository = sellerProfileRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional(readOnly = true)
    public SellerDashboardSummaryResponse getDashboardSummary(AuthenticatedUser authenticatedUser) {
        getSellerProfileEntity(authenticatedUser);
        long userId = authenticatedUser.getId();
        return new SellerDashboardSummaryResponse(
                orderItemRepository.countDistinctOrdersBySellerUserId(userId),
                productRepository.countBySellerUserId(userId),
                orderItemRepository.sumLineTotalBySellerUserId(userId)
        );
    }

    @Transactional(readOnly = true)
    public SellerProfileResponse getSellerProfile(AuthenticatedUser authenticatedUser) {
        SellerProfile sellerProfile = getSellerProfileEntity(authenticatedUser);
        return toSellerProfileResponse(sellerProfile);
    }

    public SellerProfileResponse updateSellerProfile(
            AuthenticatedUser authenticatedUser,
            UpdateSellerProfileRequest request
    ) {
        SellerProfile sellerProfile = getSellerProfileEntity(authenticatedUser);
        sellerProfile.updateStoreProfile(
                request.storeName().trim(),
                normalizeOptional(request.storeDescription())
        );
        return toSellerProfileResponse(sellerProfileRepository.save(sellerProfile));
    }

    public SellerProductResponse createProduct(
            AuthenticatedUser authenticatedUser,
            CreateProductRequest request
    ) {
        SellerProfile sellerProfile = getSellerProfileEntity(authenticatedUser);
        Category category = getCategoryEntity(request.categoryId());
        ProductStatus desiredStatus = resolveInitialStatus(sellerProfile, request.status(), request.stockQuantity());
        Product product = Product.create(
                sellerProfile,
                category,
                request.name().trim(),
                request.description().trim(),
                request.price(),
                request.stockQuantity(),
                desiredStatus,
                normalizeOptional(request.imageUrl())
        );
        return toSellerProductResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<SellerProductResponse> getOwnProducts(AuthenticatedUser authenticatedUser) {
        SellerProfile sellerProfile = getSellerProfileEntity(authenticatedUser);
        return productRepository.findAllBySellerId(sellerProfile.getId()).stream()
                .map(this::toSellerProductResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getUsedCategories(AuthenticatedUser authenticatedUser) {
        getSellerProfileEntity(authenticatedUser);
        return productRepository.findDistinctCategoriesBySellerUserId(authenticatedUser.getId()).stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SellerProductResponse getOwnProduct(AuthenticatedUser authenticatedUser, Long productId) {
        Product product = getOwnedProductEntity(authenticatedUser, productId);
        return toSellerProductResponse(product);
    }

    public SellerProductResponse updateProduct(
            AuthenticatedUser authenticatedUser,
            Long productId,
            UpdateProductRequest request
    ) {
        SellerProfile sellerProfile = getSellerProfileEntity(authenticatedUser);
        Product product = getOwnedProductEntity(authenticatedUser, productId);
        validateSellerManagedStatus(sellerProfile, request.status(), request.stockQuantity());
        Category category = getCategoryEntity(request.categoryId());

        product.updateDetails(
                category,
                request.name().trim(),
                request.description().trim(),
                request.price(),
                request.stockQuantity(),
                adjustStatusForStock(request.status(), request.stockQuantity()),
                normalizeOptional(request.imageUrl())
        );
        return toSellerProductResponse(productRepository.save(product));
    }

    public SellerProductResponse disableProduct(AuthenticatedUser authenticatedUser, Long productId) {
        Product product = getOwnedProductEntity(authenticatedUser, productId);
        product.disable();
        return toSellerProductResponse(productRepository.save(product));
    }

    public SellerProductResponse enableProduct(AuthenticatedUser authenticatedUser, Long productId) {
        SellerProfile sellerProfile = getSellerProfileEntity(authenticatedUser);
        Product product = getOwnedProductEntity(authenticatedUser, productId);

        if (product.getStatus() == ProductStatus.BLOCKED_BY_ADMIN) {
            throw new IllegalArgumentException("Blocked products cannot be enabled by seller");
        }
        validateSellerManagedStatus(sellerProfile, ProductStatus.ACTIVE, product.getStockQuantity());
        product.enable();
        return toSellerProductResponse(productRepository.save(product));
    }

    public SellerProductResponse updateStock(
            AuthenticatedUser authenticatedUser,
            Long productId,
            UpdateStockRequest request
    ) {
        SellerProfile sellerProfile = getSellerProfileEntity(authenticatedUser);
        Product product = getOwnedProductEntity(authenticatedUser, productId);
        product.updateStock(request.stockQuantity());

        if (request.stockQuantity() == 0) {
            product.updateStatus(ProductStatus.OUT_OF_STOCK);
        } else if (product.getStatus() == ProductStatus.OUT_OF_STOCK
                && sellerProfile.getApprovalStatus() == SellerApprovalStatus.APPROVED) {
            product.updateStatus(ProductStatus.ACTIVE);
        }

        return toSellerProductResponse(productRepository.save(product));
    }

    private SellerProfile getSellerProfileEntity(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.getRole() != Role.SELLER) {
            throw new IllegalArgumentException("Authenticated user is not a seller");
        }

        return sellerProfileRepository.findByUserId(authenticatedUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Seller profile not found for user id " + authenticatedUser.getId()));
    }

    private Product getOwnedProductEntity(AuthenticatedUser authenticatedUser, Long productId) {
        SellerProfile sellerProfile = getSellerProfileEntity(authenticatedUser);
        return productRepository.findByIdAndSellerId(productId, sellerProfile.getId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found for seller with id " + productId));
    }

    private Category getCategoryEntity(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found for id " + categoryId));
    }

    private ProductStatus resolveInitialStatus(
            SellerProfile sellerProfile,
            ProductStatus requestedStatus,
            Integer stockQuantity
    ) {
        ProductStatus candidateStatus = requestedStatus == null
                ? defaultStatusForCreation(sellerProfile, stockQuantity)
                : requestedStatus;
        validateSellerManagedStatus(sellerProfile, candidateStatus, stockQuantity);
        return adjustStatusForStock(candidateStatus, stockQuantity);
    }

    private ProductStatus defaultStatusForCreation(SellerProfile sellerProfile, Integer stockQuantity) {
        if (stockQuantity == 0) {
            return ProductStatus.OUT_OF_STOCK;
        }
        if (sellerProfile.getApprovalStatus() == SellerApprovalStatus.APPROVED) {
            return ProductStatus.ACTIVE;
        }
        return ProductStatus.DISABLED_BY_SELLER;
    }

    private void validateSellerManagedStatus(
            SellerProfile sellerProfile,
            ProductStatus productStatus,
            Integer stockQuantity
    ) {
        if (productStatus == ProductStatus.BLOCKED_BY_ADMIN) {
            throw new IllegalArgumentException("Seller cannot set product status to BLOCKED_BY_ADMIN");
        }
        if (productStatus == ProductStatus.ACTIVE
                && sellerProfile.getApprovalStatus() != SellerApprovalStatus.APPROVED) {
            throw new IllegalArgumentException("Seller account must be approved before activating products");
        }
        if (productStatus == ProductStatus.ACTIVE && stockQuantity == 0) {
            throw new IllegalArgumentException("Active product must have stock greater than zero");
        }
    }

    private ProductStatus adjustStatusForStock(ProductStatus productStatus, Integer stockQuantity) {
        if (stockQuantity == 0 && productStatus == ProductStatus.ACTIVE) {
            return ProductStatus.OUT_OF_STOCK;
        }
        return productStatus;
    }

    private SellerProfileResponse toSellerProfileResponse(SellerProfile sellerProfile) {
        return new SellerProfileResponse(
                sellerProfile.getId(),
                sellerProfile.getUser().getId(),
                sellerProfile.getUser().getEmail(),
                sellerProfile.getStoreName(),
                sellerProfile.getStoreDescription(),
                sellerProfile.getApprovalStatus(),
                sellerProfile.getRejectionReason()
        );
    }

    private SellerProductResponse toSellerProductResponse(Product product) {
        return new SellerProductResponse(
                product.getId(),
                product.getSeller().getId(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus(),
                product.getImageUrl()
        );
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug()
        );
    }
}
