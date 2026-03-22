package com.sample.marketplace.dto.admin;

public record AdminDashboardSummaryResponse(
        long totalUsers,
        long totalBuyers,
        long totalSellers,
        long totalAdmins,
        long bannedUsers,
        long pendingSellerApprovals,
        long approvedSellers,
        long rejectedSellers
) {
}
