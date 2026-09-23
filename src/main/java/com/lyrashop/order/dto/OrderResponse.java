package com.lyrashop.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lyrashop.order.entity.ShopOrder;

public record OrderResponse(
        UUID id,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        long loyaltyCoinsUsed,
        BigDecimal loyaltyDiscountAmount,
        BigDecimal shippingFee,
        BigDecimal giftWrapFee,
        BigDecimal totalAmount,
        String status,
        String paymentMethod,
        String paymentStatus,
        String shippingAddress,
        String shippingPhone,
        String note,
        String voucherCode,
        boolean giftWrap,
        String giftMessage,
        String cancellationReason,
        String shippingCarrier,
        String trackingCode,
        String trackingUrl,
        Instant estimatedDeliveryAt,
        Instant deliveredAt,
        Instant expiresAt,
        String returnStatus,
        String returnReason,
        Instant returnRequestedAt,
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
                order.getSubtotalAmount(),
                order.getDiscountAmount(),
                order.getLoyaltyCoinsUsed(),
                order.getLoyaltyDiscountAmount(),
                order.getShippingFee(),
                order.getGiftWrapFee(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getPaymentMethod().name(),
                order.getPaymentStatus().name(),
                order.getShippingAddress(),
                order.getShippingPhone(),
                order.getNote(),
                order.getVoucherCode(),
                order.isGiftWrap(),
                order.getGiftMessage(),
                order.getCancellationReason(),
                order.getShippingCarrier(),
                order.getTrackingCode(),
                order.getTrackingUrl(),
                order.getEstimatedDeliveryAt(),
                order.getDeliveredAt(),
                order.getExpiresAt(),
                order.getReturnStatus(),
                order.getReturnReason(),
                order.getReturnRequestedAt(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                paymentUrl
        );
    }
}
