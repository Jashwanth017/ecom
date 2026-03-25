package com.sample.marketplace.repositories;

import com.sample.marketplace.models.Category;
import com.sample.marketplace.models.Product;
import com.sample.marketplace.models.enums.ProductStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    List<Product> findAllBySellerId(Long sellerId);

    long countBySellerUserId(Long userId);

    Optional<Product> findByIdAndSellerId(Long id, Long sellerId);

    Optional<Product> findByIdAndStatus(Long id, ProductStatus status);

    @Query("select distinct p.category from Product p where p.seller.user.id = :userId order by p.category.name asc")
    List<Category> findDistinctCategoriesBySellerUserId(Long userId);
}
