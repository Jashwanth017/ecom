package com.sample.marketplace.repositories;

import com.sample.marketplace.models.BuyerAddress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyerAddressRepository extends JpaRepository<BuyerAddress, Long> {

    List<BuyerAddress> findAllByBuyerProfileUserIdOrderByCreatedAtDesc(Long userId);

    Optional<BuyerAddress> findByIdAndBuyerProfileUserId(Long id, Long userId);
}
