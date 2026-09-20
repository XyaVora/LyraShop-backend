package com.lyrashop.order.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTrackingRequest(
        @NotBlank @Size(max = 100) String carrier,
        @NotBlank @Size(max = 100) String trackingCode,
        @Size(max = 500) String trackingUrl,
        Instant estimatedDeliveryAt
) {
    public UpdateTrackingRequest {
        carrier = carrier == null ? null : carrier.strip();
        trackingCode = trackingCode == null ? null : trackingCode.strip();
        trackingUrl = trackingUrl == null || trackingUrl.isBlank() ? null : trackingUrl.strip();
    }
}
