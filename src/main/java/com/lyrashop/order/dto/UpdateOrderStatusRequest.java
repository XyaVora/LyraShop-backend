package com.lyrashop.order.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateOrderStatusRequest(
        @NotBlank String status
) {
    public UpdateOrderStatusRequest {
        status = status == null ? null : status.strip();
    }
}
