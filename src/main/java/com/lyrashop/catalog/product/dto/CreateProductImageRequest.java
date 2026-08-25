package com.lyrashop.catalog.product.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateProductImageRequest(
        @NotBlank @Size(max = 2048) String url,
        UUID variantId,
        boolean primary,
        @NotNull @PositiveOrZero Integer sortOrder
) {
    public CreateProductImageRequest {
        url = url == null ? null : url.strip();
    }
}
