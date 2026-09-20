package com.lyrashop.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.lyrashop.order.entity.OrderItem;

public record OrderItemResponse(
        Long id,
        UUID variantId,
        UUID productId,
        String productName,
        String sku,
        String size,
        String color,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getVariantId(),
                item.getProductId(),
                item.getProductName(),
                item.getSku(),
                item.getSize(),
                item.getColor(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}
