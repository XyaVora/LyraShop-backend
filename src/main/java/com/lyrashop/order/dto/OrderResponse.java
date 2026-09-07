package com.lyrashop.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lyrashop.order.entity.ShopOrder;

public record OrderResponse(
        UUID id,
        BigDecimal totalAmount,
        String status,
        String paymentMethod,
        String paymentStatus,
        String shippingAddress,
        String shippingPhone,
        String note,
        Instant createdAt,
        Instant updatedAt,
        List<OrderItemResponse> items,
        @JsonInclude(JsonInclude.Include.NON_NULL) String paymentUrl
) {
    public static OrderResponse from(ShopOrder order) {
        return from(order, null);
    }

    public static OrderResponse from(ShopOrder order, String paymentUrl) {
        return new OrderResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getPaymentMethod().name(),
                order.getPaymentStatus().name(),
                order.getShippingAddress(),
                order.getShippingPhone(),
                order.getNote(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                paymentUrl
        );
    }
}
