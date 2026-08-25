package com.lyrashop.catalog.product.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.lyrashop.catalog.product.entity.Product;

public record ProductResult(
        UUID id,
        String name,
        String slug,
        String description,
        BigDecimal basePrice,
        Long categoryId,
        Instant createdAt,
        Instant updatedAt,
        BigDecimal averageRating,
        long reviewCount
) {

    public static ProductResult from(Product product) {
        return from(product, null, 0);
    }

    public static ProductResult from(Product product, BigDecimal averageRating, long reviewCount) {
        return new ProductResult(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getBasePrice(),
                product.getCategoryId(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                averageRating,
                reviewCount
        );
    }
}