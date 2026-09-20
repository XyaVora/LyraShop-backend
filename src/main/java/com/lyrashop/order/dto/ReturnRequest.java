package com.lyrashop.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReturnRequest(@NotBlank @Size(max = 1000) String reason) {
    public ReturnRequest {
        reason = reason == null ? null : reason.strip();
    }
}
