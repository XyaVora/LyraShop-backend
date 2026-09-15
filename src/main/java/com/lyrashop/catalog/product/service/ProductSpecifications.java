package com.lyrashop.catalog.product.service;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;

import com.lyrashop.catalog.product.entity.Product;
import com.lyrashop.catalog.variant.entity.ProductVariant;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> active() {
        return (root, query, builder) -> builder.isTrue(root.get("active"));
    }

    public static Specification<Product> id(java.util.UUID id) {
        return (root, query, builder) -> builder.equal(root.get("id"), id);
    }

    public static Specification<Product> idNot(java.util.UUID id) {
        return (root, query, builder) -> builder.notEqual(root.get("id"), id);
    }

    public static Specification<Product> categoryActive() {
        return (root, query, builder) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            var category = subquery.from(com.lyrashop.catalog.category.entity.Category.class);
            subquery.select(category.get("id"))
                    .where(
                            builder.equal(category.get("id"), root.get("categoryId")),
                            builder.isTrue(category.get("active"))
                    );
            return builder.exists(subquery);
        };
    }

    public static Specification<Product> categoryId(Long categoryId) {
        return (root, query, builder) -> builder.equal(root.get("categoryId"), categoryId);
    }

    public static Specification<Product> keyword(String keyword) {
        String pattern = "%" + escapeLike(keyword.toLowerCase(Locale.ROOT)) + "%";
        boolean asciiKeyword = keyword.codePoints().allMatch(codePoint -> codePoint < 128);
        return (root, query, builder) -> {
            var name = builder.lower(root.get("name"));
            var description = builder.lower(root.get("description"));
            if (!asciiKeyword) {
                return builder.or(
                        builder.like(name, pattern, '\\'),
                        builder.like(description, pattern, '\\')
                );
            }
            var slug = builder.lower(root.get("slug"));
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

    public static Specification<Product> activeVariant(String variantSize, String color) {
        return (root, query, builder) -> {
            Subquery<java.util.UUID> subquery = query.subquery(java.util.UUID.class);
            var variant = subquery.from(ProductVariant.class);
            Predicate match = builder.and(
                    builder.equal(variant.get("productId"), root.get("id")),
                    builder.isTrue(variant.get("active"))
            );
            if (variantSize != null) {
                match = builder.and(
                        match,
                        builder.equal(builder.lower(variant.get("size")), variantSize)
                );
            }
            if (color != null) {
                match = builder.and(
                        match,
                        builder.equal(builder.lower(variant.get("color")), color)
                );
            }
            subquery.select(variant.get("id")).where(match);
            return builder.exists(subquery);
        };
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
