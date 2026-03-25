package com.sample.marketplace.controllers;

import com.sample.marketplace.dto.buyer.BuyerAddressRequest;
import com.sample.marketplace.dto.buyer.BuyerAddressResponse;
import com.sample.marketplace.dto.buyer.BuyerProfileResponse;
import com.sample.marketplace.dto.buyer.UpdateBuyerProfileRequest;
import com.sample.marketplace.security.AuthenticatedUser;
import com.sample.marketplace.services.BuyerProfileService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/buyer")
public class BuyerController {

    private final BuyerProfileService buyerProfileService;

    public BuyerController(BuyerProfileService buyerProfileService) {
        this.buyerProfileService = buyerProfileService;
    }

    @GetMapping("/profile")
    public BuyerProfileResponse getBuyerProfile(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return buyerProfileService.getBuyerProfile(authenticatedUser);
    }

    @PutMapping("/profile")
    public BuyerProfileResponse updateBuyerProfile(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody UpdateBuyerProfileRequest request
    ) {
        return buyerProfileService.updateBuyerProfile(authenticatedUser, request);
    }

    @GetMapping("/addresses")
    public List<BuyerAddressResponse> getAddresses(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return buyerProfileService.getAddresses(authenticatedUser);
    }

    @PostMapping("/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public BuyerAddressResponse addAddress(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody BuyerAddressRequest request
    ) {
        return buyerProfileService.addAddress(authenticatedUser, request);
    }

    @PutMapping("/addresses/{addressId}")
    public BuyerAddressResponse updateAddress(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long addressId,
            @Valid @RequestBody BuyerAddressRequest request
    ) {
        return buyerProfileService.updateAddress(authenticatedUser, addressId, request);
    }

    @DeleteMapping("/addresses/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddress(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long addressId
    ) {
        buyerProfileService.deleteAddress(authenticatedUser, addressId);
    }
}
