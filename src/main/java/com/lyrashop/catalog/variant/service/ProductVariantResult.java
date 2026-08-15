package com.lyrashop.catalog.variant.service;

import java.math.BigDecimal;
import java.util.UUID;

import com.lyrashop.catalog.variant.repository.PublicProductVariantProjection;

public record ProductVariantResult(
        UUID id,
        String sku,
        String size,
        String color,
        BigDecimal price,
        int stock
) {

    public static ProductVariantResult from(PublicProductVariantProjection variant) {
        return new ProductVariantResult(
                variant.getId(),
                variant.getSku(),
                variant.getSize(),
                variant.getColor(),
                variant.getPrice(),
                variant.getStock()
        );
    }
}
