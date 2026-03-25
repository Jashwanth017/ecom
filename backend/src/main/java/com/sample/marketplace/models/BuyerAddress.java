package com.sample.marketplace.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "buyer_addresses")
public class BuyerAddress extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_profile_id", nullable = false)
    private BuyerProfile buyerProfile;

    @Column(name = "address_line_1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "address_line_2", length = 255)
    private String addressLine2;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    protected BuyerAddress() {
    }

    public static BuyerAddress create(
            BuyerProfile buyerProfile,
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String postalCode
    ) {
        BuyerAddress buyerAddress = new BuyerAddress();
        buyerAddress.buyerProfile = buyerProfile;
        buyerAddress.addressLine1 = addressLine1;
        buyerAddress.addressLine2 = addressLine2;
        buyerAddress.city = city;
        buyerAddress.state = state;
        buyerAddress.postalCode = postalCode;
        return buyerAddress;
    }

    public Long getId() {
        return id;
    }

    public BuyerProfile getBuyerProfile() {
        return buyerProfile;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void update(
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String postalCode
    ) {
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
    }
}
