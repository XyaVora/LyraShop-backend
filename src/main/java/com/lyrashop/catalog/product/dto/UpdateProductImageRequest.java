package com.lyrashop.catalog.product.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateProductImageRequest(
        UUID variantId,
        boolean primary,
        @NotNull @PositiveOrZero Integer sortOrder
) {}
