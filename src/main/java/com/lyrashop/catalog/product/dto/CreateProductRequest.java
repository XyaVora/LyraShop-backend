package com.lyrashop.catalog.product.dto;

import java.math.BigDecimal;
import java.util.Locale;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = 255, message = "must not exceed 255 characters")
        String name,

        @NotBlank(message = "must not be blank")
        @Size(max = 255, message = "must not exceed 255 characters")
        @Pattern(
                regexp = "[a-z0-9]+(?:-[a-z0-9]+)*",
                message = "must contain lowercase letters, digits, and hyphens"
        )
        String slug,

        @Size(max = 2_000, message = "must not exceed 2000 characters")
        String description,

        @NotNull(message = "must not be null")
        @DecimalMin(value = "0.00", inclusive = true, message = "must be non-negative")
        @Digits(integer = 10, fraction = 2, message = "must have at most 10 integer digits and 2 fraction digits")
        BigDecimal basePrice,

        @NotNull(message = "must not be null")
        @Positive(message = "must be positive")
        Long categoryId
) {

    public CreateProductRequest {
        name = stripNullable(name);
        slug = stripNullable(slug);
        if (slug != null) {
            slug = slug.toLowerCase(Locale.ROOT);
        }
        description = stripNullable(description);
        if (basePrice != null && (basePrice.signum() < 0 || basePrice.scale() < 0 || basePrice.scale() > 2
                || basePrice.precision() - basePrice.scale() > 10 || basePrice.precision() > 12)) {
            throw new IllegalArgumentException("basePrice must be a non-negative amount with at most two decimals");
        }
    }

    private static String stripNullable(String value) {
        return value == null ? null : value.strip();
    }
}
