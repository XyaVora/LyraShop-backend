package com.lyrashop.cart.dto;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(
        @NotNull UUID variantId,
        @NotNull @Min(1) @Max(99) Integer quantity
) {
}
