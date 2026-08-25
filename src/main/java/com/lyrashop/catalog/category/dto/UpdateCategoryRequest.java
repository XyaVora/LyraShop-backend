package com.lyrashop.catalog.category.dto;

import java.util.Locale;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = 100, message = "must not exceed 100 characters")
        String name,

        @NotBlank(message = "must not be blank")
        @Size(max = 100, message = "must not exceed 100 characters")
        @Pattern(
                regexp = "[a-z0-9]+(?:-[a-z0-9]+)*",
                message = "must contain lowercase letters, digits, and hyphens"
        )
        String slug,

        @Size(max = 2_000, message = "must not exceed 2000 characters")
        String description,

        @Positive(message = "must be positive")
        Long parentId
) {

    public UpdateCategoryRequest {
        name = stripNullable(name);
        slug = stripNullable(slug);
        if (slug != null) {
            slug = slug.toLowerCase(Locale.ROOT);
        }
        description = stripNullable(description);
    }

    private static String stripNullable(String value) {
        return value == null ? null : value.strip();
    }
}
