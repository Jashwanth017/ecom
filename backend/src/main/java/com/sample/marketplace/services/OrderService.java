package com.sample.marketplace.services;

import com.sample.marketplace.dto.order.BuyerOrderDetailResponse;
import com.sample.marketplace.dto.order.BuyerOrderSummaryResponse;
import com.sample.marketplace.dto.order.OrderDeliveryAddressResponse;
import com.sample.marketplace.dto.order.OrderItemResponse;
import com.sample.marketplace.dto.order.SellerOrderItemResponse;
import com.sample.marketplace.models.BuyerAddress;
import com.sample.marketplace.models.BuyerProfile;
import com.sample.marketplace.models.Cart;
import com.sample.marketplace.models.CartItem;
import com.sample.marketplace.models.Order;
import com.sample.marketplace.models.OrderItem;
import com.sample.marketplace.models.Product;
import com.sample.marketplace.models.enums.ProductStatus;
import com.sample.marketplace.models.enums.Role;
import com.sample.marketplace.repositories.BuyerProfileRepository;
import com.sample.marketplace.repositories.BuyerAddressRepository;
import com.sample.marketplace.repositories.CartItemRepository;
import com.sample.marketplace.repositories.CartRepository;
import com.sample.marketplace.repositories.OrderItemRepository;
import com.sample.marketplace.repositories.OrderRepository;
import com.sample.marketplace.repositories.ProductRepository;
import com.sample.marketplace.security.AuthenticatedUser;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {

    private final BuyerProfileRepository buyerProfileRepository;
    private final BuyerAddressRepository buyerAddressRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public OrderService(
            BuyerProfileRepository buyerProfileRepository,
            BuyerAddressRepository buyerAddressRepository,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository
    ) {
        this.buyerProfileRepository = buyerProfileRepository;
        this.buyerAddressRepository = buyerAddressRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    public BuyerOrderDetailResponse placeOrder(AuthenticatedUser authenticatedUser, Long addressId) {
        BuyerProfile buyerProfile = getBuyerProfile(authenticatedUser);
        BuyerAddress deliveryAddress = getOwnedBuyerAddress(authenticatedUser, addressId);
        Cart cart = cartRepository.findByBuyerUserId(authenticatedUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cart is empty"));
        List<CartItem> cartItems = cartItemRepository.findAllByCartId(cart.getId());

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        BigDecimal totalAmount = cartItems.stream()
                .map(item -> calculateLineTotal(item.getProduct(), item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = orderRepository.save(Order.create(
                buyerProfile,
                totalAmount,
                deliveryAddress.getAddressLine1(),
                deliveryAddress.getAddressLine2(),
                deliveryAddress.getCity(),
                deliveryAddress.getState(),
                deliveryAddress.getPostalCode()
        ));

        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> createOrderItem(order, cartItem))
                .toList();

        orderItemRepository.saveAll(orderItems);
        cartItemRepository.deleteAll(cartItems);

        return toBuyerOrderDetailResponse(order, orderItems);
    }

    @Transactional(readOnly = true)
    public List<BuyerOrderSummaryResponse> getBuyerOrders(AuthenticatedUser authenticatedUser) {
        ensureBuyerRole(authenticatedUser);
        return orderRepository.findAllByBuyerUserIdOrderByPlacedAtDesc(authenticatedUser.getId()).stream()
                .map(order -> toBuyerOrderSummaryResponse(order, orderItemRepository.findAllByOrderId(order.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public BuyerOrderDetailResponse getBuyerOrderDetail(AuthenticatedUser authenticatedUser, Long orderId) {
        ensureBuyerRole(authenticatedUser);
        Order order = orderRepository.findByIdAndBuyerUserId(orderId, authenticatedUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found for id " + orderId));
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(order.getId());
        return toBuyerOrderDetailResponse(order, orderItems);
    }

    @Transactional(readOnly = true)
    public List<SellerOrderItemResponse> getSellerOrderItems(AuthenticatedUser authenticatedUser) {
        ensureSellerRole(authenticatedUser);
        return orderItemRepository.findAllBySellerUserIdOrderByCreatedAtDesc(authenticatedUser.getId()).stream()
                .map(this::toSellerOrderItemResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SellerOrderItemResponse getSellerOrderItem(AuthenticatedUser authenticatedUser, Long orderItemId) {
        ensureSellerRole(authenticatedUser);
        OrderItem orderItem = orderItemRepository.findByIdAndSellerUserId(orderItemId, authenticatedUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Seller order item not found for id " + orderItemId));
        return toSellerOrderItemResponse(orderItem);
    }

    private OrderItem createOrderItem(Order order, CartItem cartItem) {
        Product product = productRepository.findById(cartItem.getProduct().getId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found for id " + cartItem.getProduct().getId()));

        validatePurchasableProduct(product, cartItem.getQuantity());
        product.decreaseStock(cartItem.getQuantity());
        if (product.getStockQuantity() == 0) {
            product.updateStatus(ProductStatus.OUT_OF_STOCK);
        }
        productRepository.save(product);
        return OrderItem.create(order, product, cartItem.getQuantity());
    }

    private BuyerProfile getBuyerProfile(AuthenticatedUser authenticatedUser) {
        ensureBuyerRole(authenticatedUser);
        return buyerProfileRepository.findByUserId(authenticatedUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Buyer profile not found for user id " + authenticatedUser.getId()));
    }

    private BuyerAddress getOwnedBuyerAddress(AuthenticatedUser authenticatedUser, Long addressId) {
        ensureBuyerRole(authenticatedUser);
        return buyerAddressRepository.findByIdAndBuyerProfileUserId(addressId, authenticatedUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Buyer address not found for id " + addressId));
    }

    private void ensureBuyerRole(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.getRole() != Role.BUYER) {
            throw new IllegalArgumentException("Authenticated user is not a buyer");
        }
    }

    private void ensureSellerRole(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.getRole() != Role.SELLER) {
            throw new IllegalArgumentException("Authenticated user is not a seller");
        }
    }

    private void validatePurchasableProduct(Product product, Integer quantity) {
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalArgumentException("Product is not available for purchase");
        }
        if (quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock during order placement");
        }
    }

    private BigDecimal calculateLineTotal(Product product, Integer quantity) {
        validatePurchasableProduct(product, quantity);
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    private BuyerOrderSummaryResponse toBuyerOrderSummaryResponse(Order order, List<OrderItem> orderItems) {
        return new BuyerOrderSummaryResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getPlacedAt(),
                orderItems.size()
        );
    }

    private BuyerOrderDetailResponse toBuyerOrderDetailResponse(Order order, List<OrderItem> orderItems) {
        return new BuyerOrderDetailResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getPlacedAt(),
                new OrderDeliveryAddressResponse(
                        order.getDeliveryAddressLine1(),
                        order.getDeliveryAddressLine2(),
                        order.getDeliveryCity(),
                        order.getDeliveryState(),
                        order.getDeliveryPostalCode()
                ),
                orderItems.stream().map(this::toOrderItemResponse).toList()
        );
    }

    private OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getProduct().getId(),
                orderItem.getProductNameSnapshot(),
                orderItem.getProductPriceSnapshot(),
                orderItem.getQuantity(),
                orderItem.getLineTotal()
        );
    }

    private SellerOrderItemResponse toSellerOrderItemResponse(OrderItem orderItem) {
        return new SellerOrderItemResponse(
                orderItem.getId(),
                orderItem.getOrder().getId(),
                orderItem.getProduct().getId(),
                orderItem.getProductNameSnapshot(),
                orderItem.getProductPriceSnapshot(),
                orderItem.getQuantity(),
                orderItem.getLineTotal(),
                orderItem.getOrder().getStatus(),
                orderItem.getOrder().getPlacedAt()
        );
    }
}
