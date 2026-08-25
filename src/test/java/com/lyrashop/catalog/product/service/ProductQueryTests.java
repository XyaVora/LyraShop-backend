package com.lyrashop.catalog.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ProductQueryTests {

    @Test
    void normalizesVariantFiltersWithoutChangingPageSize() {
        ProductQuery query = ProductQuery.from(
                null, null, "  M  ", "Black", null, null, null, "0", "20"
        );

        assertThat(query.variantSize()).isEqualTo("m");
        assertThat(query.color()).isEqualTo("black");
        assertThat(query.size()).isEqualTo(20);
        assertThat(query.page()).isZero();
    }

    @Test
    void treatsBlankVariantFiltersAsAbsent() {
        ProductQuery query = ProductQuery.from(
                null, null, "   ", "", null, null, null, null, null
        );

        assertThat(query.variantSize()).isNull();
        assertThat(query.color()).isNull();
        assertThat(query.size()).isEqualTo(ProductQuery.DEFAULT_SIZE);
    }

    @Test
    void rejectsOversizedVariantFilters() {
        assertThatThrownBy(() -> ProductQuery.from(
                null, null, "m".repeat(21), null, null, null, null, null, null
        )).isInstanceOf(ProductQueryException.class);
        assertThatThrownBy(() -> ProductQuery.from(
                null, null, null, "c".repeat(51), null, null, null, null, null
        )).isInstanceOf(ProductQueryException.class);
    }
}
