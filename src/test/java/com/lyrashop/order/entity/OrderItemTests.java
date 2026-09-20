package com.lyrashop.order.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class OrderItemTests {

    @Test
    void snapshotsProductAndVariantIdentifiersForOrderHistoryActions() {
        UUID variantId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderItem item = OrderItem.snapshot(
                variantId,
                productId,
                "Linen Shirt",
                "SHIRT-M-WHITE",
                "M",
                "White",
                2,
                new BigDecimal("450000.00"),
                new BigDecimal("900000.00")
        );

        assertThat(item.getVariantId()).isEqualTo(variantId);
        assertThat(item.getProductId()).isEqualTo(productId);
    }
}
