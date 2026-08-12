package com.lyrashop.catalog.product.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.lyrashop.catalog.product.service.ProductResult;

public record ProductResponse(
        UUID id,
        String name,
        String slug,
        String description,
        BigDecimal basePrice,
        Long categoryId,
        Instant createdAt,
        Instant updatedAt
) {

    public static ProductResponse from(ProductResult product) {
        return new ProductResponse(
                product.id(),
                product.name(),
                product.slug(),
                product.description(),
                product.basePrice(),
                product.categoryId(),
                product.createdAt(),
                product.updatedAt()
        );
    }
}