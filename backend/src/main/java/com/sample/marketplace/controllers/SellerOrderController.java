package com.sample.marketplace.controllers;

import com.sample.marketplace.dto.order.SellerOrderItemResponse;
import com.sample.marketplace.security.AuthenticatedUser;
import com.sample.marketplace.services.OrderService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/seller/orders/items")
public class SellerOrderController {

    private final OrderService orderService;

    public SellerOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<SellerOrderItemResponse> getSellerOrderItems(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return orderService.getSellerOrderItems(authenticatedUser);
    }

    @GetMapping("/{orderItemId}")
    public SellerOrderItemResponse getSellerOrderItem(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long orderItemId
    ) {
        return orderService.getSellerOrderItem(authenticatedUser, orderItemId);
    }
}
