package com.sample.marketplace.models;

import com.sample.marketplace.models.enums.ProductStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private SellerProfile seller;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductStatus status;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    protected Product() {
    }

    public static Product create(
            SellerProfile seller,
            Category category,
            String name,
            String description,
            BigDecimal price,
            Integer stockQuantity,
            ProductStatus status,
            String imageUrl
    ) {
        Product product = new Product();
        product.seller = seller;
        product.category = category;
        product.name = name;
        product.description = description;
        product.price = price;
        product.stockQuantity = stockQuantity;
        product.status = status;
        product.imageUrl = imageUrl;
        return product;
    }

    public Long getId() {
        return id;
    }

    public SellerProfile getSeller() {
        return seller;
    }

    public Category getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void updateDetails(
            Category category,
            String name,
            String description,
            BigDecimal price,
            Integer stockQuantity,
            ProductStatus status,
            String imageUrl
    ) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.status = status;
        this.imageUrl = imageUrl;
    }

    public void updateStock(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public void decreaseStock(Integer quantity) {
        this.stockQuantity = this.stockQuantity - quantity;
    }

    public void updateStatus(ProductStatus status) {
        this.status = status;
    }

    public void disable() {
        this.status = ProductStatus.DISABLED_BY_SELLER;
    }

    public void enable() {
        this.status = ProductStatus.ACTIVE;
    }
}
