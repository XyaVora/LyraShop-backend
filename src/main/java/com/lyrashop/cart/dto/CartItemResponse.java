package com.lyrashop.cart.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        Long id,
        UUID variantId,
        String sku,
        String size,
        String color,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal
) {
}
