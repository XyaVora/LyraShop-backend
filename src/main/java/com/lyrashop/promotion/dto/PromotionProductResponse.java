package com.lyrashop.promotion.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.lyrashop.promotion.entity.PromotionProduct;

public record PromotionProductResponse(UUID productId, BigDecimal salePrice, BigDecimal originalPrice,
        int discountPercent) {
    public static PromotionProductResponse from(PromotionProduct item) {
        return new PromotionProductResponse(item.getProductId(), item.getSalePrice(), item.getOriginalPrice(),
                item.getDiscountPercent());
    }
}
