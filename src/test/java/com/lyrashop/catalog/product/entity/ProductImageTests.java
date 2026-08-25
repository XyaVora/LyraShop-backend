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
}
