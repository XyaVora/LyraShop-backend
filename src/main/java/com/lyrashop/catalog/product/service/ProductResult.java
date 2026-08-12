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
        Instant updatedAt
) {

    public static ProductResult from(Product product) {
        return new ProductResult(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getBasePrice(),
                product.getCategoryId(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}