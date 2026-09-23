package com.lyrashop.order.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.lyrashop.order.entity.CustomerReturnEvidence;
import com.lyrashop.order.entity.CustomerReturnItem;
import com.lyrashop.order.entity.CustomerReturnRequest;

public record ReturnRequestResponse(UUID id, UUID orderId, String status, String reason, Instant createdAt,
        List<Item> items, List<String> evidenceUrls) {
    public record Item(Long orderItemId, int quantity) {}
    public static ReturnRequestResponse from(CustomerReturnRequest request, List<CustomerReturnItem> items,
            List<CustomerReturnEvidence> evidence) {
        return new ReturnRequestResponse(request.getId(), request.getOrderId(), request.getStatus(),
                request.getReason(), request.getCreatedAt(),
                items.stream().map(item -> new Item(item.getOrderItemId(), item.getQuantity())).toList(),
                evidence.stream().map(CustomerReturnEvidence::getUrl).toList());
    }
}
