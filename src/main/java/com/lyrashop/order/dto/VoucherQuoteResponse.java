package com.lyrashop.order.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record VoucherQuoteResponse(
        String code,
        String label,
        BigDecimal discountAmount,
        BigDecimal shippingFee,
        BigDecimal totalAmount,
        String type,
        String discountText,
        BigDecimal minimumOrderAmount,
        boolean eligible,
        Instant expiresAt
) {}
