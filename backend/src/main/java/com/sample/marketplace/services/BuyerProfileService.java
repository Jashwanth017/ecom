package com.sample.marketplace.services;

import com.sample.marketplace.dto.buyer.BuyerProfileResponse;
import com.sample.marketplace.dto.buyer.BuyerAddressRequest;
import com.sample.marketplace.dto.buyer.BuyerAddressResponse;
import com.sample.marketplace.dto.buyer.UpdateBuyerProfileRequest;
import com.sample.marketplace.models.BuyerAddress;
import com.sample.marketplace.models.BuyerProfile;
import com.sample.marketplace.models.enums.Role;
import com.sample.marketplace.repositories.BuyerAddressRepository;
import com.sample.marketplace.repositories.BuyerProfileRepository;
import com.sample.marketplace.security.AuthenticatedUser;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BuyerProfileService {

    private final BuyerProfileRepository buyerProfileRepository;
    private final BuyerAddressRepository buyerAddressRepository;

    public BuyerProfileService(
            BuyerProfileRepository buyerProfileRepository,
            BuyerAddressRepository buyerAddressRepository
    ) {
        this.buyerProfileRepository = buyerProfileRepository;
        this.buyerAddressRepository = buyerAddressRepository;
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
                normalizeOptional(request.phone())
        );
        return toBuyerProfileResponse(buyerProfileRepository.save(buyerProfile));
    }

    public BuyerAddressResponse addAddress(
            AuthenticatedUser authenticatedUser,
            BuyerAddressRequest request
    ) {
        BuyerProfile buyerProfile = getBuyerProfileEntity(authenticatedUser);
        BuyerAddress buyerAddress = BuyerAddress.create(
                buyerProfile,
                request.addressLine1().trim(),
                normalizeOptional(request.addressLine2()),
                request.city().trim(),
                request.state().trim(),
                request.postalCode().trim()
        );
        return toBuyerAddressResponse(buyerAddressRepository.save(buyerAddress));
    }

    public BuyerAddressResponse updateAddress(
            AuthenticatedUser authenticatedUser,
            Long addressId,
            BuyerAddressRequest request
    ) {
        BuyerAddress buyerAddress = getOwnedAddressEntity(authenticatedUser, addressId);
        buyerAddress.update(
                request.addressLine1().trim(),
                normalizeOptional(request.addressLine2()),
                request.city().trim(),
                request.state().trim(),
                request.postalCode().trim()
        );
        return toBuyerAddressResponse(buyerAddressRepository.save(buyerAddress));
    }

    public void deleteAddress(AuthenticatedUser authenticatedUser, Long addressId) {
        BuyerAddress buyerAddress = getOwnedAddressEntity(authenticatedUser, addressId);
        buyerAddressRepository.delete(buyerAddress);
    }

    @Transactional(readOnly = true)
    public List<BuyerAddressResponse> getAddresses(AuthenticatedUser authenticatedUser) {
        getBuyerProfileEntity(authenticatedUser);
        return buyerAddressRepository.findAllByBuyerProfileUserIdOrderByCreatedAtDesc(authenticatedUser.getId())
                .stream()
                .map(this::toBuyerAddressResponse)
                .toList();
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
                buyerProfile.getAddresses().stream()
                        .map(this::toBuyerAddressResponse)
                        .toList()
        );
    }

    private BuyerAddress getOwnedAddressEntity(AuthenticatedUser authenticatedUser, Long addressId) {
        if (authenticatedUser.getRole() != Role.BUYER) {
            throw new IllegalArgumentException("Authenticated user is not a buyer");
        }
        return buyerAddressRepository.findByIdAndBuyerProfileUserId(addressId, authenticatedUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Buyer address not found for id " + addressId));
    }

    private BuyerAddressResponse toBuyerAddressResponse(BuyerAddress buyerAddress) {
        return new BuyerAddressResponse(
                buyerAddress.getId(),
                buyerAddress.getAddressLine1(),
                buyerAddress.getAddressLine2(),
                buyerAddress.getCity(),
                buyerAddress.getState(),
                buyerAddress.getPostalCode()
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
