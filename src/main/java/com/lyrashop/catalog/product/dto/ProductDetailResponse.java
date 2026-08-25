package com.lyrashop.catalog.product.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.lyrashop.catalog.product.service.ProductDetailResult;
import com.lyrashop.catalog.variant.dto.PublicProductVariantResponse;

public record ProductDetailResponse(
        UUID id,
        String name,
        String slug,
        String description,
        BigDecimal basePrice,
        Long categoryId,
        Instant createdAt,
        Instant updatedAt,
        List<PublicProductVariantResponse> variants,
        List<ProductImageResponse> images
) {

    public static ProductDetailResponse from(ProductDetailResult detail) {
        var product = detail.product();
        return new ProductDetailResponse(
                product.id(),
                product.name(),
                product.slug(),
                product.description(),
                product.basePrice(),
                product.categoryId(),
                product.createdAt(),
                product.updatedAt(),
                detail.variants().stream().map(PublicProductVariantResponse::from).toList(),
                detail.images().stream().map(ProductImageResponse::from).toList()
        );
    }
}
