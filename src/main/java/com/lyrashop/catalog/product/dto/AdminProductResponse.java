package com.lyrashop.catalog.product.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.lyrashop.catalog.product.entity.Product;

public record AdminProductResponse(
        UUID id,
        String name,
        String slug,
        String description,
        BigDecimal basePrice,
        Long categoryId,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    public static AdminProductResponse from(Product product) {
        return new AdminProductResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getBasePrice(),
                product.getCategoryId(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
