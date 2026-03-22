package com.sample.marketplace.services;

import com.sample.marketplace.dto.cart.AddCartItemRequest;
import com.sample.marketplace.dto.cart.CartItemResponse;
import com.sample.marketplace.dto.cart.CartResponse;
import com.sample.marketplace.dto.cart.UpdateCartItemRequest;
import com.sample.marketplace.models.BuyerProfile;
import com.sample.marketplace.models.Cart;
import com.sample.marketplace.models.CartItem;
import com.sample.marketplace.models.Product;
import com.sample.marketplace.models.enums.ProductStatus;
import com.sample.marketplace.models.enums.Role;
import com.sample.marketplace.repositories.BuyerProfileRepository;
import com.sample.marketplace.repositories.CartItemRepository;
import com.sample.marketplace.repositories.CartRepository;
import com.sample.marketplace.repositories.ProductRepository;
import com.sample.marketplace.security.AuthenticatedUser;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartService {

    private final BuyerProfileRepository buyerProfileRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(
            BuyerProfileRepository buyerProfileRepository,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository
    ) {
        this.buyerProfileRepository = buyerProfileRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public CartResponse getCurrentCart(AuthenticatedUser authenticatedUser) {
        Cart cart = getOrCreateCart(authenticatedUser);
        return toCartResponse(cart);
    }

    public CartResponse addItem(AuthenticatedUser authenticatedUser, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(authenticatedUser);
        Product product = getPurchasableProduct(request.productId());
        validateQuantityAgainstStock(product, request.quantity());

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .map(existingItem -> {
                    int updatedQuantity = existingItem.getQuantity() + request.quantity();
                    validateQuantityAgainstStock(product, updatedQuantity);
                    existingItem.updateQuantity(updatedQuantity);
                    return existingItem;
                })
                .orElseGet(() -> CartItem.create(cart, product, request.quantity()));

        cartItemRepository.save(cartItem);
        return toCartResponse(cart);
    }

    public CartResponse updateItemQuantity(
            AuthenticatedUser authenticatedUser,
            Long cartItemId,
            UpdateCartItemRequest request
    ) {
        Cart cart = getOrCreateCart(authenticatedUser);
        CartItem cartItem = getOwnedCartItem(cart, cartItemId);
        Product product = getPurchasableProduct(cartItem.getProduct().getId());
        validateQuantityAgainstStock(product, request.quantity());
        cartItem.updateQuantity(request.quantity());
        cartItemRepository.save(cartItem);
        return toCartResponse(cart);
    }

    public CartResponse removeItem(AuthenticatedUser authenticatedUser, Long cartItemId) {
        Cart cart = getOrCreateCart(authenticatedUser);
        CartItem cartItem = getOwnedCartItem(cart, cartItemId);
        cartItemRepository.delete(cartItem);
        return toCartResponse(cart);
    }

    private Cart getOrCreateCart(AuthenticatedUser authenticatedUser) {
        BuyerProfile buyerProfile = getBuyerProfile(authenticatedUser);
        return cartRepository.findByBuyerUserId(authenticatedUser.getId())
                .orElseGet(() -> cartRepository.save(Cart.create(buyerProfile)));
    }

    private BuyerProfile getBuyerProfile(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.getRole() != Role.BUYER) {
            throw new IllegalArgumentException("Authenticated user is not a buyer");
        }
        return buyerProfileRepository.findByUserId(authenticatedUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Buyer profile not found for user id " + authenticatedUser.getId()));
    }

    private Product getPurchasableProduct(Long productId) {
        return productRepository.findByIdAndStatus(productId, ProductStatus.ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("Active product not found for id " + productId));
    }

    private CartItem getOwnedCartItem(Cart cart, Long cartItemId) {
        return cartItemRepository.findByIdAndCartId(cartItemId, cart.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found for id " + cartItemId));
    }

    private void validateQuantityAgainstStock(Product product, Integer quantity) {
        if (quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock");
        }
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cartItemRepository.findAllByCartId(cart.getId()).stream()
                .map(this::toCartItemResponse)
                .toList();

        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getId(),
                cart.getBuyer().getId(),
                itemResponses,
                totalAmount
        );
    }

    private CartItemResponse toCartItemResponse(CartItem cartItem) {
        BigDecimal lineTotal = cartItem.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return new CartItemResponse(
                cartItem.getId(),
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getProduct().getImageUrl(),
                cartItem.getProduct().getPrice(),
                cartItem.getQuantity(),
                lineTotal
        );
    }
}
