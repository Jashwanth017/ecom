package com.sample.marketplace.controllers;

import com.sample.marketplace.dto.buyer.BuyerProfileResponse;
import com.sample.marketplace.dto.buyer.UpdateBuyerProfileRequest;
import com.sample.marketplace.security.AuthenticatedUser;
import com.sample.marketplace.services.BuyerProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
