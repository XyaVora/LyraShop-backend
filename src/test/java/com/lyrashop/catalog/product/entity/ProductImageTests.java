package com.lyrashop.catalog.product.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ProductImageTests {

    @Test
    void acceptsHttpsUrlsAndRejectsUnsafeSchemes() {
        assertThat(ProductImage.normalizeUrl(" https://cdn.example.test/shirt.jpg "))
                .isEqualTo("https://cdn.example.test/shirt.jpg");
        assertThatThrownBy(() -> ProductImage.normalizeUrl("http://cdn.example.test/shirt.jpg"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ProductImage.normalizeUrl("javascript:alert(1)"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ProductImage.normalizeUrl("https://user:pass@cdn.example.test/shirt.jpg"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsStoredFilePathsAndRejectsTraversal() {
        assertThat(ProductImage.normalizeUrl(
                " /api/v1/files/550e8400-E29B-41D4-A716-446655440000.JPG "
        )).isEqualTo("/api/v1/files/550e8400-e29b-41d4-a716-446655440000.jpg");
        assertThatThrownBy(() -> ProductImage.normalizeUrl("/api/v1/files/../secret.jpg"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ProductImage.normalizeUrl("/api/v1/files/not-a-uuid.png"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
