package com.sample.marketplace.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "buyer_profiles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_buyer_profiles_user", columnNames = "user_id")
        }
)
public class BuyerProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @OneToMany(mappedBy = "buyerProfile", fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    private List<BuyerAddress> addresses = new ArrayList<>();

    protected BuyerProfile() {
    }

    public static BuyerProfile create(User user, String fullName, String phone) {
        BuyerProfile profile = new BuyerProfile();
        profile.user = user;
        profile.fullName = fullName;
        profile.phone = phone;
        return profile;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public List<BuyerAddress> getAddresses() {
        return addresses;
    }

    public void updateProfile(String fullName, String phone) {
        this.fullName = fullName;
        this.phone = phone;
    }
}
