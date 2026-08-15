package com.lyrashop.catalog.variant.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.lyrashop.catalog.variant.service.ProductVariantResult;

public record PublicProductVariantResponse(
        UUID id,
        String sku,
        String size,
        String color,
        BigDecimal price,
        int stock
) {

    public static PublicProductVariantResponse from(ProductVariantResult variant) {
        return new PublicProductVariantResponse(
                variant.id(),
                variant.sku(),
                variant.size(),
                variant.color(),
                variant.price(),
                variant.stock()
        );
    }
}
