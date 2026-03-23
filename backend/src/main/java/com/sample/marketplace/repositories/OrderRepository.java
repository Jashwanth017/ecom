package com.sample.marketplace.repositories;

import com.sample.marketplace.models.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByBuyerUserIdOrderByPlacedAtDesc(Long userId);

    Optional<Order> findByIdAndBuyerUserId(Long orderId, Long userId);
}
