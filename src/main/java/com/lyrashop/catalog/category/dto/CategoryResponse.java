package com.lyrashop.catalog.category.dto;

import java.time.Instant;

import com.lyrashop.catalog.category.service.CategoryResult;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        Long parentId,
        Instant createdAt,
        Instant updatedAt,
        long productCount
) {

    public static CategoryResponse from(CategoryResult category) {
        return from(category, 0);
    }

    public static CategoryResponse from(CategoryResult category, long productCount) {
        return new CategoryResponse(
                category.id(),
                category.name(),
                category.slug(),
                category.description(),
                category.parentId(),
                category.createdAt(),
                category.updatedAt(),
                productCount
        );
    }
}
