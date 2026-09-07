package com.lyrashop.catalog.category.dto;

import java.time.Instant;

import com.lyrashop.catalog.category.service.CategoryResult;

public record AdminCategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        Long parentId,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    public static AdminCategoryResponse from(CategoryResult category) {
        return new AdminCategoryResponse(
                category.id(),
                category.name(),
                category.slug(),
                category.description(),
                category.parentId(),
                category.active(),
                category.createdAt(),
                category.updatedAt()
        );
    }
}
