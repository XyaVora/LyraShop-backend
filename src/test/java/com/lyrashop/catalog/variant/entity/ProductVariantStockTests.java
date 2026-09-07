package com.lyrashop.catalog.variant.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ProductVariantStockTests {

    @Test
    void decrementsAndRejectsOverdraft() {
        ProductVariant variant = ProductVariant.create(
                UUID.randomUUID(), "STOCK-1", "M", "Black", new BigDecimal("10.00"), 4
        );
        variant.decrementStock(3);
        assertThat(variant.getStock()).isEqualTo(1);
        assertThatThrownBy(() -> variant.decrementStock(2))
                .isInstanceOf(IllegalArgumentException.class);
        variant.incrementStock(2);
        assertThat(variant.getStock()).isEqualTo(3);
        variant.deactivate();
        assertThat(variant.isActive()).isFalse();
        variant.activate();
        assertThat(variant.isActive()).isTrue();
    }
}
