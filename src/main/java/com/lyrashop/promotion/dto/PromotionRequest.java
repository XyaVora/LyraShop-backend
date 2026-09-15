package com.lyrashop.promotion.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PromotionRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 2000) String description,
        @Min(1) @Max(100) int discountPercent,
        @NotNull Instant startsAt,
        @NotNull @Future Instant endsAt,
        boolean active,
        @NotNull @Size(max = 100) List<UUID> productIds
) {}
