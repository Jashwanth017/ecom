package com.sample.marketplace.controllers;

import com.sample.marketplace.dto.cart.AddCartItemRequest;
import com.sample.marketplace.dto.cart.CartResponse;
import com.sample.marketplace.dto.cart.UpdateCartItemRequest;
import com.sample.marketplace.security.AuthenticatedUser;
import com.sample.marketplace.services.CartService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/buyer/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse getCurrentCart(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return cartService.getCurrentCart(authenticatedUser);
    }

    @PostMapping("/items")
    public CartResponse addItem(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        return cartService.addItem(authenticatedUser, request);
    }

    @PutMapping("/items/{cartItemId}")
    public CartResponse updateItemQuantity(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return cartService.updateItemQuantity(authenticatedUser, cartItemId, request);
    }

    @DeleteMapping("/items/{cartItemId}")
    public CartResponse removeItem(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long cartItemId
    ) {
        return cartService.removeItem(authenticatedUser, cartItemId);
    }
}
