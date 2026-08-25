package com.lyrashop.catalog.variant.dto;

import java.time.Instant;
import java.util.UUID;

import com.lyrashop.catalog.variant.entity.ProductVariant;

public record ProductVariantInventoryResponse(
        UUID variantId,
        int stock,
        long version,
        Instant updatedAt
) {
    public static ProductVariantInventoryResponse from(ProductVariant variant) {
        return new ProductVariantInventoryResponse(
                variant.getId(),
                variant.getStock(),
                variant.getVersion(),
                variant.getUpdatedAt()
        );
    }
}
