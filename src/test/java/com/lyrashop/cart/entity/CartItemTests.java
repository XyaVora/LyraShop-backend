package com.lyrashop.cart.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class CartItemTests {

    @Test
    void rejectsNonPositiveQuantity() {
        UUID cartId = UUID.randomUUID();
        UUID variantId = UUID.randomUUID();
        CartItem item = CartItem.create(cartId, variantId, 2);
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThatThrownBy(() -> CartItem.create(cartId, variantId, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> item.setQuantity(0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
