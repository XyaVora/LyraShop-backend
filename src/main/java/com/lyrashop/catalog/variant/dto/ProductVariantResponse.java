package com.lyrashop.catalog.variant.dto;

import java.math.BigDecimal; import java.time.Instant; import java.util.UUID;
import com.lyrashop.catalog.variant.entity.ProductVariant;

public record ProductVariantResponse(UUID id, UUID productId, String sku, String size, String color, BigDecimal price, int stock, long version, Instant createdAt, Instant updatedAt) {
 public static ProductVariantResponse from(ProductVariant v){ return new ProductVariantResponse(v.getId(),v.getProductId(),v.getSku(),v.getSize(),v.getColor(),v.getPrice(),v.getStock(),v.getVersion(),v.getCreatedAt(),v.getUpdatedAt()); }
}
