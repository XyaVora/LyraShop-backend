package com.lyrashop.review.repository;

import java.util.UUID;

public interface ProductReviewSummary {

    UUID getProductId();

    Double getAverageRating();

    Long getReviewCount();
}
