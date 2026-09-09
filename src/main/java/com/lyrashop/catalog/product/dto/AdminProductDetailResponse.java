package com.lyrashop.catalog.product.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.lyrashop.catalog.product.entity.Product;
import com.lyrashop.catalog.product.entity.ProductImage;
import com.lyrashop.catalog.variant.entity.ProductVariant;

public record AdminProductDetailResponse(
        UUID id,
        String name,
        String slug,
        String description,
        BigDecimal basePrice,
        Long categoryId,
        boolean active,
        long version,
        Instant createdAt,
        Instant updatedAt,
        List<AdminProductVariantResponse> variants,
        List<ProductImageResponse> images
) {

    public static AdminProductDetailResponse from(
            Product product,
            List<ProductVariant> variants,
            List<ProductImage> images
    ) {
        return new AdminProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getBasePrice(),
                product.getCategoryId(),
                product.isActive(),
                product.getVersion(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                variants.stream().map(AdminProductVariantResponse::from).toList(),
                images.stream().map(ProductImageResponse::from).toList()
        );
    }
}
