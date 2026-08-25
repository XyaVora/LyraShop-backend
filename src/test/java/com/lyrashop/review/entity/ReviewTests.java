package com.lyrashop.review.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class ReviewTests {

    @Test
    void acceptsRatingsFromOneToFive() {
        Review review = Review.create(UUID.randomUUID(), UUID.randomUUID(), 5, "  Great  ");
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getComment()).isEqualTo("Great");
        assertThatThrownBy(() -> Review.create(UUID.randomUUID(), UUID.randomUUID(), 0, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Review.create(UUID.randomUUID(), UUID.randomUUID(), 6, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
