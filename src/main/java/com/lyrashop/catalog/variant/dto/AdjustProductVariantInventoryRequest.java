package com.lyrashop.catalog.variant.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AdjustProductVariantInventoryRequest(
        @NotNull @PositiveOrZero Integer stock,
        @NotNull @PositiveOrZero Long version
) {
}
