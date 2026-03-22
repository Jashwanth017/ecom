package com.sample.marketplace.services;

import com.sample.marketplace.dto.buyer.BuyerProfileResponse;
import com.sample.marketplace.dto.buyer.UpdateBuyerProfileRequest;
import com.sample.marketplace.models.BuyerProfile;
import com.sample.marketplace.models.enums.Role;
import com.sample.marketplace.repositories.BuyerProfileRepository;
import com.sample.marketplace.security.AuthenticatedUser;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BuyerProfileService {

    private final BuyerProfileRepository buyerProfileRepository;

    public BuyerProfileService(BuyerProfileRepository buyerProfileRepository) {
        this.buyerProfileRepository = buyerProfileRepository;
    }

    @Transactional(readOnly = true)
    public BuyerProfileResponse getBuyerProfile(AuthenticatedUser authenticatedUser) {
        BuyerProfile buyerProfile = getBuyerProfileEntity(authenticatedUser);
        return toBuyerProfileResponse(buyerProfile);
    }

    public BuyerProfileResponse updateBuyerProfile(
            AuthenticatedUser authenticatedUser,
            UpdateBuyerProfileRequest request
    ) {
        BuyerProfile buyerProfile = getBuyerProfileEntity(authenticatedUser);
        buyerProfile.updateProfile(
                request.fullName().trim(),
                normalizeOptional(request.phone()),
                normalizeOptional(request.addressLine1()),
                normalizeOptional(request.addressLine2()),
                normalizeOptional(request.city()),
                normalizeOptional(request.state()),
                normalizeOptional(request.postalCode())
        );
        return toBuyerProfileResponse(buyerProfileRepository.save(buyerProfile));
    }

    private BuyerProfile getBuyerProfileEntity(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.getRole() != Role.BUYER) {
            throw new IllegalArgumentException("Authenticated user is not a buyer");
        }

        return buyerProfileRepository.findByUserId(authenticatedUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Buyer profile not found for user id " + authenticatedUser.getId()));
    }

    private BuyerProfileResponse toBuyerProfileResponse(BuyerProfile buyerProfile) {
        return new BuyerProfileResponse(
                buyerProfile.getId(),
                buyerProfile.getUser().getId(),
                buyerProfile.getUser().getEmail(),
                buyerProfile.getFullName(),
                buyerProfile.getPhone(),
                buyerProfile.getAddressLine1(),
                buyerProfile.getAddressLine2(),
                buyerProfile.getCity(),
                buyerProfile.getState(),
                buyerProfile.getPostalCode()
        );
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
