package com.lyrashop.order.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ShopOrderTests {

    @Test
    void allowsPendingCancellationAndForwardStatusTransitions() {
        ShopOrder order = ShopOrder.create(
                UUID.randomUUID(),
                new BigDecimal("10.00"),
                PaymentMethod.COD,
                "12 Test Street",
                "0900000000",
                null
        );
        order.cancel();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThatThrownBy(order::cancel).isInstanceOf(IllegalStateException.class);

        ShopOrder progressing = ShopOrder.create(
                UUID.randomUUID(),
                new BigDecimal("10.00"),
                PaymentMethod.COD,
                "12 Test Street",
                "0900000000",
                null
        );
        progressing.transitionTo(OrderStatus.CONFIRMED);
        progressing.transitionTo(OrderStatus.PROCESSING);
        progressing.transitionTo(OrderStatus.SHIPPING);
        progressing.transitionTo(OrderStatus.DELIVERED);
        assertThat(progressing.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(progressing.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThatThrownBy(() -> progressing.transitionTo(OrderStatus.PENDING))
                .isInstanceOf(IllegalStateException.class);
    }
}
