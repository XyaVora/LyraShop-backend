package com.lyrashop.dashboard.dto;

import java.util.List;

public record DashboardResponse(
        DashboardRevenueResponse revenue,
        DashboardOrderCountsResponse orders,
        List<DashboardBestSellerResponse> bestSellers
) {
}
