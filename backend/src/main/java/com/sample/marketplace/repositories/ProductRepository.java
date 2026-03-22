package com.sample.marketplace.repositories;

import com.sample.marketplace.models.Product;
import com.sample.marketplace.models.enums.ProductStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    List<Product> findAllBySellerId(Long sellerId);

    Optional<Product> findByIdAndSellerId(Long id, Long sellerId);

    Optional<Product> findByIdAndStatus(Long id, ProductStatus status);
}
