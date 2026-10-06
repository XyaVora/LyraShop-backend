package com.lyrashop.review.dto;

import java.time.Instant;
import java.util.UUID;

import com.lyrashop.review.entity.Review;

public record ReviewResponse(
        Long id,
        UUID productId,
        UUID userId,
        int rating,
        String comment,
        String moderationStatus,
        String moderationNote,
        UUID moderatedBy,
        Instant moderatedAt,
        Instant createdAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getProductId(),
                review.getUserId(),
                review.getRating(),
                review.getComment(),
                review.getModerationStatus(),
                review.getModerationNote(),
                review.getModeratedBy(),
                review.getModeratedAt(),
                review.getCreatedAt()
        );
    }
}
