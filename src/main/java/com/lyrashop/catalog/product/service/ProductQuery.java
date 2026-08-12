package com.lyrashop.catalog.product.service;

import java.math.BigDecimal;
import java.util.Locale;

import org.springframework.data.domain.Sort;

import com.lyrashop.catalog.category.entity.Category;

public record ProductQuery(
        String keyword,
        String categorySlug,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        int page,
        int size,
        ProductSort sort
) {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public static ProductQuery from(
            String keyword,
            String category,
            String minPrice,
            String maxPrice,
            String sort,
            String page,
            String size
    ) {
        int parsedPage = parsePage(page);
        int parsedSize = parseSize(size);
        BigDecimal parsedMin = parsePrice(minPrice);
        BigDecimal parsedMax = parsePrice(maxPrice);
        if (parsedMin != null && parsedMax != null && parsedMin.compareTo(parsedMax) > 0) {
            throw new ProductQueryException();
        }
        String normalizedKeyword = normalizeKeyword(keyword);
        String normalizedCategory = normalizeCategory(category);
        return new ProductQuery(
                normalizedKeyword,
                normalizedCategory,
                parsedMin,
                parsedMax,
                parsedPage,
                parsedSize,
                ProductSort.parse(sort)
        );
    }

    private static String normalizeKeyword(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.strip();
        if (normalized.length() > 100) throw new ProductQueryException();
        return normalized;
    }

    private static String normalizeCategory(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Category.normalizeSlug(value.strip());
        } catch (IllegalArgumentException exception) {
            throw new ProductQueryException();
        }
    }

    private static BigDecimal parsePrice(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            BigDecimal price = new BigDecimal(value.strip());
            if (price.signum() < 0 || price.scale() < 0 || price.scale() > 2
                    || price.precision() - price.scale() > 10 || price.precision() > 12) {
                throw new ProductQueryException();
            }
            return price;
        } catch (NumberFormatException exception) {
            throw new ProductQueryException();
        }
    }

    private static int parsePage(String value) {
        int page = parseInt(value, 0);
        if (page < 0) throw new ProductQueryException();
        return page;
    }

    private static int parseSize(String value) {
        int size = parseInt(value, DEFAULT_SIZE);
        if (size < 1 || size > MAX_SIZE) throw new ProductQueryException();
        return size;
    }

    private static int parseInt(String value, int fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return Integer.parseInt(value.strip());
        } catch (NumberFormatException exception) {
            throw new ProductQueryException();
        }
    }

    public record ProductSort(String property, Sort.Direction direction) {

        public static ProductSort parse(String value) {
            if (value == null || value.isBlank()) {
                return new ProductSort("createdAt", Sort.Direction.DESC);
            }
            String[] parts = value.strip().split(",", -1);
            if (parts.length > 2 || parts[0].isBlank()) throw new ProductQueryException();
            String external = parts[0].strip().toLowerCase(Locale.ROOT);
            String property = switch (external) {
                case "name" -> "name";
                case "slug" -> "slug";
                case "price", "baseprice", "base_price" -> "basePrice";
                case "createdat", "created_at" -> "createdAt";
                default -> throw new ProductQueryException();
            };
            Sort.Direction direction = parts.length == 1
                    ? Sort.Direction.ASC
                    : parseDirection(parts[1]);
            return new ProductSort(property, direction);
        }

        private static Sort.Direction parseDirection(String value) {
            try {
                return Sort.Direction.fromString(value.strip());
            } catch (IllegalArgumentException exception) {
                throw new ProductQueryException();
            }
        }

        public Sort toSort() {
            return Sort.by(new Sort.Order(direction, property))
                    .and(Sort.by(Sort.Order.asc("id")));
        }
    }
}