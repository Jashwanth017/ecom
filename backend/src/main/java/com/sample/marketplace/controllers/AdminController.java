package com.sample.marketplace.controllers;

import com.sample.marketplace.dto.admin.AdminDashboardSummaryResponse;
import com.sample.marketplace.dto.admin.AdminUserResponse;
import com.sample.marketplace.dto.admin.SellerApprovalActionRequest;
import com.sample.marketplace.models.enums.Role;
import com.sample.marketplace.models.enums.UserStatus;
import com.sample.marketplace.services.AdminService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard/summary")
    public AdminDashboardSummaryResponse getDashboardSummary() {
        return adminService.getDashboardSummary();
    }

    @GetMapping("/users")
    public List<AdminUserResponse> getUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status
    ) {
        return adminService.getUsers(role, status);
    }

    @GetMapping("/users/{userId}")
    public AdminUserResponse getUser(@PathVariable Long userId) {
        return adminService.getUser(userId);
    }

    @PatchMapping("/users/{userId}/ban")
    public AdminUserResponse banUser(@PathVariable Long userId) {
        return adminService.banUser(userId);
    }

    @PatchMapping("/users/{userId}/unban")
    public AdminUserResponse unbanUser(@PathVariable Long userId) {
        return adminService.unbanUser(userId);
    }

    @GetMapping("/sellers/pending")
    public List<AdminUserResponse> getPendingSellers() {
        return adminService.getPendingSellers();
    }

    @PatchMapping("/sellers/{sellerProfileId}/approve")
    public AdminUserResponse approveSeller(@PathVariable Long sellerProfileId) {
        return adminService.approveSeller(sellerProfileId);
    }

    @PatchMapping("/sellers/{sellerProfileId}/reject")
    public AdminUserResponse rejectSeller(
            @PathVariable Long sellerProfileId,
            @Valid @RequestBody(required = false) SellerApprovalActionRequest request
    ) {
        return adminService.rejectSeller(sellerProfileId, request == null ? null : request.reason());
    }
}
