package com.sample.marketplace.repositories;

import com.sample.marketplace.models.OrderItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findAllByOrderId(Long orderId);

    List<OrderItem> findAllBySellerUserIdOrderByCreatedAtDesc(Long userId);

    Optional<OrderItem> findByIdAndSellerUserId(Long orderItemId, Long userId);
}
