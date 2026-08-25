package com.lyrashop.dashboard.dto;

public record DashboardOrderCountsResponse(
        long total,
        long pending,
        long confirmed,
        long processing,
        long shipping,
        long delivered,
        long cancelled
) {
}
