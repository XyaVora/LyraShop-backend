package com.lyrashop.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.lyrashop.order.entity.OrderRefund;

public record RefundResponse(UUID id, UUID orderId, UUID returnRequestId, BigDecimal amount,
        String reference, String note, UUID processedBy, Instant createdAt) {
    public static RefundResponse from(OrderRefund refund) {
        return new RefundResponse(refund.getId(), refund.getOrderId(), refund.getReturnRequestId(),
                refund.getAmount(), refund.getReference(), refund.getNote(), refund.getProcessedBy(),
                refund.getCreatedAt());
    }
}
