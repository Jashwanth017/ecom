package com.sample.marketplace.repositories;

import com.sample.marketplace.models.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllBySellerId(Long sellerId);

    Optional<Product> findByIdAndSellerId(Long id, Long sellerId);
}
