package com.lyrashop.catalog.product.dto;

import java.util.UUID;

import com.lyrashop.catalog.product.entity.ProductImage;

public record ProductImageResponse(
        Long id,
        UUID variantId,
        String url,
        boolean primary,
        int sortOrder
) {
    public static ProductImageResponse from(ProductImage image) {
        return new ProductImageResponse(
                image.getId(),
                image.getVariantId(),
                image.getUrl(),
                image.isPrimary(),
                image.getSortOrder()
        );
    }
}
