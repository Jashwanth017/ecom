package com.sample.marketplace.controllers;

import com.sample.marketplace.dto.auth.AuthResponse;
import com.sample.marketplace.dto.auth.BuyerRegistrationRequest;
import com.sample.marketplace.dto.auth.CurrentUserResponse;
import com.sample.marketplace.dto.auth.LoginRequest;
import com.sample.marketplace.dto.auth.LogoutRequest;
import com.sample.marketplace.dto.auth.RegistrationResponse;
import com.sample.marketplace.dto.auth.RefreshTokenRequest;
import com.sample.marketplace.dto.auth.SellerRegistrationRequest;
import com.sample.marketplace.security.AuthenticatedUser;
import com.sample.marketplace.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/buyer")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse registerBuyer(@Valid @RequestBody BuyerRegistrationRequest request) {
        return authService.registerBuyer(request);
    }

    @PostMapping("/register/seller")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse registerSeller(@Valid @RequestBody SellerRegistrationRequest request) {
        return authService.registerSeller(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshAccessToken(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
    }

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return authService.getCurrentUser(authenticatedUser);
    }
}
