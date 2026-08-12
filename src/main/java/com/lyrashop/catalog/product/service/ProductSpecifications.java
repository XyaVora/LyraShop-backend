package com.lyrashop.catalog.product.service;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.lyrashop.catalog.product.entity.Product;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> active() {
        return (root, query, builder) -> builder.isTrue(root.get("active"));
    }

    public static Specification<Product> categoryId(Long categoryId) {
        return (root, query, builder) -> builder.equal(root.get("categoryId"), categoryId);
    }

    public static Specification<Product> keyword(String keyword) {
        String pattern = "%" + escapeLike(keyword.toLowerCase(Locale.ROOT)) + "%";
        return (root, query, builder) -> {
            var name = builder.lower(root.get("name"));
            var slug = builder.lower(root.get("slug"));
            var description = builder.lower(root.get("description"));
            return builder.or(
                    builder.like(name, pattern, '\\'),
                    builder.like(slug, pattern, '\\'),
                    builder.like(description, pattern, '\\')
            );
        };
    }

    public static Specification<Product> minPrice(java.math.BigDecimal price) {
        return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("basePrice"), price);
    }

    public static Specification<Product> maxPrice(java.math.BigDecimal price) {
        return (root, query, builder) -> builder.lessThanOrEqualTo(root.get("basePrice"), price);
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}