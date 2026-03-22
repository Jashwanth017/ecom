package com.sample.marketplace.models;

import com.sample.marketplace.models.enums.SellerApprovalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
        name = "seller_profiles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_seller_profiles_user", columnNames = "user_id")
        }
)
public class SellerProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "store_name", nullable = false, length = 150)
    private String storeName;

    @Column(name = "store_description", columnDefinition = "TEXT")
    private String storeDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private SellerApprovalStatus approvalStatus;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    protected SellerProfile() {
    }

    public static SellerProfile create(User user, String storeName, String storeDescription) {
        SellerProfile profile = new SellerProfile();
        profile.user = user;
        profile.storeName = storeName;
        profile.storeDescription = storeDescription;
        profile.approvalStatus = SellerApprovalStatus.PENDING;
        return profile;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getStoreDescription() {
        return storeDescription;
    }

    public SellerApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void updateStoreProfile(String storeName, String storeDescription) {
        this.storeName = storeName;
        this.storeDescription = storeDescription;
    }

    public void approve() {
        this.approvalStatus = SellerApprovalStatus.APPROVED;
        this.approvedAt = Instant.now();
        this.rejectionReason = null;
    }

    public void reject(String rejectionReason) {
        this.approvalStatus = SellerApprovalStatus.REJECTED;
        this.approvedAt = null;
        this.rejectionReason = rejectionReason;
    }
}
