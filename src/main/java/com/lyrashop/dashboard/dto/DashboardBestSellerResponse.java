package com.lyrashop.dashboard.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DashboardBestSellerResponse(
        UUID productId,
        String productName,
        long quantitySold,
        BigDecimal revenue
) {
}
