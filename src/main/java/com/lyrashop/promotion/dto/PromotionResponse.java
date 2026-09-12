package com.lyrashop.promotion.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.lyrashop.promotion.entity.Promotion;

public record PromotionResponse(UUID id, String title, String subtitle, String description, String badge,
        Instant startsAt, Instant endsAt, List<PromotionProductResponse> items) {
    public static PromotionResponse from(Promotion promotion, List<PromotionProductResponse> items) {
        return new PromotionResponse(promotion.getId(), promotion.getTitle(), promotion.getSubtitle(),
                promotion.getDescription(), promotion.getBadge(), promotion.getStartsAt(), promotion.getEndsAt(), items);
    }
}
