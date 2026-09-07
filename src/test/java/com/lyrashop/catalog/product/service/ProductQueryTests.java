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
    void treatsNonNumericSizeAsClothingSizeForGoalClients() {
        ProductQuery query = ProductQuery.from(
                null, null, null, null, null, null, null, "0", "M", null, null
        );

        assertThat(query.variantSize()).isEqualTo("m");
        assertThat(query.size()).isEqualTo(ProductQuery.DEFAULT_SIZE);
    }

    @Test
    void prefersExplicitPageSizeAndClothingSizeAliases() {
        ProductQuery query = ProductQuery.from(
                null, null, null, null, null, null, null, "1", "M", "L", "10"
        );

        assertThat(query.variantSize()).isEqualTo("l");
        assertThat(query.size()).isEqualTo(10);
        assertThat(query.page()).isEqualTo(1);
    }

    @Test
    void keepsNumericSizeAsPageSize() {
        ProductQuery query = ProductQuery.from(
                null, null, "XL", null, null, null, null, null, "15"
        );

        assertThat(query.variantSize()).isEqualTo("xl");
        assertThat(query.size()).isEqualTo(15);
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
