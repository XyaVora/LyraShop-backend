package com.lyrashop.catalog.category.service;

import java.time.Instant;

import com.lyrashop.catalog.category.entity.Category;

public record CategoryResult(
        Long id,
        String name,
        String slug,
        String description,
        Long parentId,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    public static CategoryResult from(Category category) {
        return new CategoryResult(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getParentId(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
