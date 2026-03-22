package com.sample.marketplace.services;

import com.sample.marketplace.dto.catalog.CatalogProductSummaryResponse;
import com.sample.marketplace.dto.catalog.CategoryResponse;
import com.sample.marketplace.dto.catalog.ProductDetailResponse;
import com.sample.marketplace.models.Category;
import com.sample.marketplace.models.Product;
import com.sample.marketplace.models.enums.ProductStatus;
import com.sample.marketplace.repositories.CategoryRepository;
import com.sample.marketplace.repositories.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public CatalogService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<CatalogProductSummaryResponse> getProducts(String search, Long categoryId, String sort) {
        Specification<Product> specification = buildCatalogSpecification(search, categoryId);
        Sort sortOrder = resolveSort(sort);

        return productRepository.findAll(specification, sortOrder).stream()
                .map(this::toCatalogProductSummaryResponse)
                .toList();
    }

    public ProductDetailResponse getProductDetail(Long productId) {
        Product product = productRepository.findByIdAndStatus(productId, ProductStatus.ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("Public product not found for id " + productId));
        return toProductDetailResponse(product);
    }

    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    private Specification<Product> buildCatalogSpecification(String search, Long categoryId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("status"), ProductStatus.ACTIVE));

            if (search != null && !search.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + search.trim().toLowerCase() + "%"
                ));
            }

            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        return switch (sort.trim().toLowerCase()) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "name_asc" -> Sort.by(Sort.Direction.ASC, "name");
            case "name_desc" -> Sort.by(Sort.Direction.DESC, "name");
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            default -> throw new IllegalArgumentException("Unsupported sort option: " + sort);
        };
    }

    private CatalogProductSummaryResponse toCatalogProductSummaryResponse(Product product) {
        return new CatalogProductSummaryResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getName(),
                product.getPrice(),
                product.getImageUrl()
        );
    }

    private ProductDetailResponse toProductDetailResponse(Product product) {
        return new ProductDetailResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getSeller().getStoreName(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUrl()
        );
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug()
        );
    }
}
