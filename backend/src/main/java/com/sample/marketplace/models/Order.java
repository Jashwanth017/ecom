package com.sample.marketplace.models;

import com.sample.marketplace.models.enums.OrderStatus;
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
import java.time.Instant;

@Entity
@Table(name = "orders")
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private BuyerProfile buyer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "placed_at", nullable = false)
    private Instant placedAt;

    @Column(name = "delivery_address_line_1", nullable = false, length = 255)
    private String deliveryAddressLine1;

    @Column(name = "delivery_address_line_2", length = 255)
    private String deliveryAddressLine2;

    @Column(name = "delivery_city", nullable = false, length = 100)
    private String deliveryCity;

    @Column(name = "delivery_state", nullable = false, length = 100)
    private String deliveryState;

    @Column(name = "delivery_postal_code", nullable = false, length = 20)
    private String deliveryPostalCode;

    protected Order() {
    }

    public static Order create(
            BuyerProfile buyer,
            BigDecimal totalAmount,
            String deliveryAddressLine1,
            String deliveryAddressLine2,
            String deliveryCity,
            String deliveryState,
            String deliveryPostalCode
    ) {
        Order order = new Order();
        order.buyer = buyer;
        order.status = OrderStatus.PLACED;
        order.totalAmount = totalAmount;
        order.placedAt = Instant.now();
        order.deliveryAddressLine1 = deliveryAddressLine1;
        order.deliveryAddressLine2 = deliveryAddressLine2;
        order.deliveryCity = deliveryCity;
        order.deliveryState = deliveryState;
        order.deliveryPostalCode = deliveryPostalCode;
        return order;
    }

    public Long getId() {
        return id;
    }

    public BuyerProfile getBuyer() {
        return buyer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Instant getPlacedAt() {
        return placedAt;
    }

    public String getDeliveryAddressLine1() {
        return deliveryAddressLine1;
    }

    public String getDeliveryAddressLine2() {
        return deliveryAddressLine2;
    }

    public String getDeliveryCity() {
        return deliveryCity;
    }

    public String getDeliveryState() {
        return deliveryState;
    }

    public String getDeliveryPostalCode() {
        return deliveryPostalCode;
    }
}
