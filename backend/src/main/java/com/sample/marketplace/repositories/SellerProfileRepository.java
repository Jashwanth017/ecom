package com.sample.marketplace.repositories;

import com.sample.marketplace.models.SellerProfile;
import com.sample.marketplace.models.enums.SellerApprovalStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {

    Optional<SellerProfile> findByUserId(Long userId);

    List<SellerProfile> findAllByApprovalStatus(SellerApprovalStatus approvalStatus);

    long countByApprovalStatus(SellerApprovalStatus approvalStatus);
}
