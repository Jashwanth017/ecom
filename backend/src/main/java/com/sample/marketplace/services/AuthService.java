package com.sample.marketplace.services;

import com.sample.marketplace.dto.auth.AuthResponse;
import com.sample.marketplace.dto.auth.BuyerRegistrationRequest;
import com.sample.marketplace.dto.auth.CurrentUserResponse;
import com.sample.marketplace.dto.auth.LoginRequest;
import com.sample.marketplace.dto.auth.RegistrationResponse;
import com.sample.marketplace.dto.auth.RefreshTokenRequest;
import com.sample.marketplace.dto.auth.SellerRegistrationRequest;
import com.sample.marketplace.exception.DuplicateAccountException;
import com.sample.marketplace.models.RefreshToken;
import com.sample.marketplace.models.SellerProfile;
import com.sample.marketplace.models.User;
import com.sample.marketplace.models.enums.Role;
import com.sample.marketplace.models.enums.SellerApprovalStatus;
import com.sample.marketplace.security.AuthenticatedUser;
import com.sample.marketplace.security.JwtTokenService;
import com.sample.marketplace.security.MarketplaceUserDetailsService;
import java.util.Locale;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserFoundationService userFoundationService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final MarketplaceUserDetailsService marketplaceUserDetailsService;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserFoundationService userFoundationService,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            MarketplaceUserDetailsService marketplaceUserDetailsService,
            JwtTokenService jwtTokenService,
            RefreshTokenService refreshTokenService
    ) {
        this.userFoundationService = userFoundationService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.marketplaceUserDetailsService = marketplaceUserDetailsService;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
    }

    public RegistrationResponse registerBuyer(BuyerRegistrationRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        validateRoleAccountDoesNotExist(normalizedEmail, Role.BUYER);

        User user = userFoundationService.createBuyerUser(
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                request.fullName().trim(),
                normalizeOptionalValue(request.phone())
        );

        return new RegistrationResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                null,
                "Buyer account registered successfully"
        );
    }

    public RegistrationResponse registerSeller(SellerRegistrationRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        validateRoleAccountDoesNotExist(normalizedEmail, Role.SELLER);

        User user = userFoundationService.createSellerUser(
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                request.storeName().trim(),
                normalizeOptionalValue(request.storeDescription())
        );
        SellerProfile sellerProfile = userFoundationService.getSellerProfileByUserId(user.getId());

        return new RegistrationResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                sellerProfile.getApprovalStatus(),
                "Seller account registered successfully and is pending approval"
        );
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        String principal = request.role().name() + ":" + normalizedEmail;

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(principal, request.password())
        );

        if (!(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            throw new BadCredentialsException("Invalid email, role, or password");
        }

        User user = userFoundationService.findUserByEmailAndRole(normalizedEmail, request.role())
                .orElseThrow(() -> new BadCredentialsException("Invalid email, role, or password"));
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return buildAuthResponse(
                authenticatedUser,
                refreshToken.getToken(),
                "Login successful"
        );
    }

    public AuthResponse refreshAccessToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.refreshToken());
        User user = refreshToken.getUser();
        AuthenticatedUser authenticatedUser = marketplaceUserDetailsService.loadUserByEmailAndRole(
                user.getEmail(),
                user.getRole()
        );

        RefreshToken rotatedRefreshToken = refreshTokenService.createRefreshToken(user);
        refreshToken.revoke();
        return buildAuthResponse(
                authenticatedUser,
                rotatedRefreshToken.getToken(),
                "Token refreshed successfully"
        );
    }

    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(AuthenticatedUser authenticatedUser) {
        return new CurrentUserResponse(
                authenticatedUser.getId(),
                authenticatedUser.getEmail(),
                authenticatedUser.getRole(),
                authenticatedUser.getStatus(),
                resolveSellerApprovalStatus(authenticatedUser)
        );
    }

    private AuthResponse buildAuthResponse(
            AuthenticatedUser authenticatedUser,
            String refreshToken,
            String message
    ) {
        return new AuthResponse(
                jwtTokenService.generateAccessToken(authenticatedUser),
                refreshToken,
                "Bearer",
                jwtTokenService.accessTokenExpiresIn(),
                jwtTokenService.refreshTokenExpiresIn(),
                authenticatedUser.getId(),
                authenticatedUser.getEmail(),
                authenticatedUser.getRole(),
                authenticatedUser.getStatus(),
                resolveSellerApprovalStatus(authenticatedUser),
                resolveRedirectPath(authenticatedUser.getRole()),
                message
        );
    }

    private void validateRoleAccountDoesNotExist(String email, Role role) {
        if (userFoundationService.accountExists(email, role)) {
            throw new DuplicateAccountException("An account already exists for this email and role");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeOptionalValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private SellerApprovalStatus resolveSellerApprovalStatus(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.getRole() != Role.SELLER) {
            return null;
        }
        return userFoundationService.getSellerProfileByUserId(authenticatedUser.getId()).getApprovalStatus();
    }

    private String resolveRedirectPath(Role role) {
        return switch (role) {
            case BUYER -> "/buyer/dashboard";
            case SELLER -> "/seller/dashboard";
            case ADMIN -> "/admin/dashboard";
        };
    }
}
