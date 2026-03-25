package com.sample.marketplace.repositories;

import com.sample.marketplace.models.OrderItem;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findAllByOrderId(Long orderId);

    List<OrderItem> findAllBySellerUserIdOrderByCreatedAtDesc(Long userId);

    Optional<OrderItem> findByIdAndSellerUserId(Long orderItemId, Long userId);

    @Query("select count(distinct oi.order.id) from OrderItem oi where oi.seller.user.id = :userId")
    long countDistinctOrdersBySellerUserId(Long userId);

    @Query("select coalesce(sum(oi.lineTotal), 0) from OrderItem oi where oi.seller.user.id = :userId")
    BigDecimal sumLineTotalBySellerUserId(Long userId);
}
