package com.lyrashop.promotion.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.lyrashop.promotion.entity.Promotion;

public record PromotionResponse(UUID id, String title, String subtitle, String description, String badge,
        int discountPercent, Instant startsAt, Instant endsAt, boolean active, List<PromotionProductResponse> items) {
    public static PromotionResponse from(Promotion promotion, List<PromotionProductResponse> items) {
        return new PromotionResponse(promotion.getId(), promotion.getName(), null,
                promotion.getDescription(), null, promotion.getDiscountPercent(),
                promotion.getStartsAt(), promotion.getEndsAt(), promotion.isActive(), items);
    }
}
