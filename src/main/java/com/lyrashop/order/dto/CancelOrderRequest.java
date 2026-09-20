package com.lyrashop.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelOrderRequest(@NotBlank @Size(max = 500) String reason) {
    public CancelOrderRequest {
        reason = reason == null ? null : reason.strip();
    }
}
