package com.sample.marketplace.controllers;

import com.sample.marketplace.dto.order.BuyerOrderDetailResponse;
import com.sample.marketplace.dto.order.BuyerOrderSummaryResponse;
import com.sample.marketplace.security.AuthenticatedUser;
import com.sample.marketplace.services.OrderService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/buyer/orders")
public class BuyerOrderController {

    private final OrderService orderService;

    public BuyerOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public BuyerOrderDetailResponse placeOrder(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return orderService.placeOrder(authenticatedUser);
    }

    @GetMapping
    public List<BuyerOrderSummaryResponse> getOrders(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return orderService.getBuyerOrders(authenticatedUser);
    }

    @GetMapping("/{orderId}")
    public BuyerOrderDetailResponse getOrderDetail(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long orderId
    ) {
        return orderService.getBuyerOrderDetail(authenticatedUser, orderId);
    }
}
