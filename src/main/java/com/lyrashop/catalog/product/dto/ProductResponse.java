package com.lyrashop.catalog.product.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.List;

import com.lyrashop.catalog.product.service.ProductResult;
import com.lyrashop.catalog.product.entity.ProductImage;
import com.lyrashop.catalog.variant.dto.PublicProductVariantResponse;
import com.lyrashop.catalog.variant.entity.ProductVariant;
import com.lyrashop.catalog.variant.service.ProductVariantResult;

public record ProductResponse(
        UUID id,
        String name,
        String slug,
        String description,
        BigDecimal basePrice,
        Long categoryId,
        Instant createdAt,
        Instant updatedAt,
        BigDecimal averageRating,
        long reviewCount,
        List<PublicProductVariantResponse> variants,
        List<ProductImageResponse> images
) {

    public ProductResponse {
        variants = variants == null ? List.of() : List.copyOf(variants);
        images = images == null ? List.of() : List.copyOf(images);
    }

    public static ProductResponse from(ProductResult product) {
        return from(product, List.of(), List.of());
    }

    public static ProductResponse from(
            ProductResult product,
            List<ProductVariant> variants,
            List<ProductImage> images
    ) {
        return new ProductResponse(
                product.id(),
                product.name(),
                product.slug(),
                product.description(),
                product.basePrice(),
                product.categoryId(),
                product.createdAt(),
                product.updatedAt(),
                product.averageRating(),
                product.reviewCount(),
                variants.stream()
                        .map(ProductVariantResult::from)
                        .map(PublicProductVariantResponse::from)
                        .toList(),
                images.stream().map(ProductImageResponse::from).toList()
        );
    }
}
