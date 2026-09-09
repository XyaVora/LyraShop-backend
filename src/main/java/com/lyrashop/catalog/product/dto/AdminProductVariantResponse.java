package com.lyrashop.catalog.product.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.lyrashop.catalog.variant.entity.ProductVariant;

public record AdminProductVariantResponse(
        UUID id,
        UUID productId,
        String sku,
        String size,
        String color,
        BigDecimal price,
        int stock,
        boolean active,
        long version,
        Instant createdAt,
        Instant updatedAt
) {

    public static AdminProductVariantResponse from(ProductVariant variant) {
        return new AdminProductVariantResponse(
                variant.getId(),
                variant.getProductId(),
                variant.getSku(),
                variant.getSize(),
                variant.getColor(),
                variant.getPrice(),
                variant.getStock(),
                variant.isActive(),
                variant.getVersion(),
                variant.getCreatedAt(),
                variant.getUpdatedAt()
        );
    }
}
