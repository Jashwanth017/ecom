package com.sample.marketplace.models;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "carts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_carts_buyer", columnNames = "buyer_id")
        }
)
public class Cart extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private BuyerProfile buyer;

    protected Cart() {
    }

    public static Cart create(BuyerProfile buyer) {
        Cart cart = new Cart();
        cart.buyer = buyer;
        return cart;
    }

    public Long getId() {
        return id;
    }

    public BuyerProfile getBuyer() {
        return buyer;
    }
}
