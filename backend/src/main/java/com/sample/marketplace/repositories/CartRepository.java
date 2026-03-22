package com.sample.marketplace.repositories;

import com.sample.marketplace.models.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByBuyerUserId(Long userId);
}
