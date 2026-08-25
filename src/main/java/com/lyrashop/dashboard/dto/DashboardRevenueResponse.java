package com.lyrashop.dashboard.dto;

import java.math.BigDecimal;

public record DashboardRevenueResponse(
        BigDecimal paidTotal,
        long paidOrderCount
) {
}
