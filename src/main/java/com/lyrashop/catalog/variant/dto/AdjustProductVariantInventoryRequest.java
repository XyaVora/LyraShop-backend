package com.lyrashop.catalog.variant.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdjustProductVariantInventoryRequest(
        @NotNull @PositiveOrZero Integer stock,
        @NotNull @PositiveOrZero Long version,
        @NotBlank @Size(max = 255) String reason
) {
}
