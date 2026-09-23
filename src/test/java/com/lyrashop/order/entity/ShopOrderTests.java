package com.lyrashop.order.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
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

    @Test
    void marksVnPayOrdersPaidUntilCancelled() {
        ShopOrder order = ShopOrder.create(
                UUID.randomUUID(),
                new BigDecimal("10.00"),
                PaymentMethod.VNPAY,
                "12 Test Street",
                "0900000000",
                null
        );
        order.markPaid();
        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        ShopOrder cancelled = ShopOrder.create(
                UUID.randomUUID(),
                new BigDecimal("10.00"),
                PaymentMethod.VNPAY,
                "12 Test Street",
                "0900000000",
                null
        );
        cancelled.cancel();
        assertThatThrownBy(cancelled::markPaid).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void appliesLoyaltyCoinsToDiscountAndPayableTotal() {
        ShopOrder order = ShopOrder.create(
                UUID.randomUUID(),
                BigDecimal.ZERO.setScale(2),
                PaymentMethod.COD,
                "12 Test Street",
                "0900000000",
                null
        );
        order.assignPricing(
                new BigDecimal("500000.00"),
                new BigDecimal("50000.00"),
                BigDecimal.ZERO.setScale(2),
                BigDecimal.ZERO.setScale(2),
                new BigDecimal("450000.00"),
                "LYRA10"
        );

        order.applyLoyalty(90000);

        assertThat(order.getLoyaltyCoinsUsed()).isEqualTo(90000);
        assertThat(order.getLoyaltyDiscountAmount()).isEqualByComparingTo("90000.00");
        assertThat(order.getDiscountAmount()).isEqualByComparingTo("140000.00");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("360000.00");
    }

    @Test
    void rejectsLoyaltyDiscountAbovePayableTotal() {
        ShopOrder order = ShopOrder.create(
                UUID.randomUUID(),
                new BigDecimal("10000.00"),
                PaymentMethod.COD,
                "12 Test Street",
                "0900000000",
                null
        );

        assertThatThrownBy(() -> order.applyLoyalty(10001))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requiresOnlinePaymentBeforeOrderConfirmationButAllowsCod() {
        ShopOrder online = ShopOrder.create(
                UUID.randomUUID(), new BigDecimal("10000.00"), PaymentMethod.VNPAY,
                "12 Test Street", "0900000000", null
        );
        assertThatThrownBy(() -> online.transitionTo(OrderStatus.CONFIRMED))
                .isInstanceOf(IllegalStateException.class);

        online.markPaid();
        online.transitionTo(OrderStatus.CONFIRMED);
        assertThat(online.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        ShopOrder cod = ShopOrder.create(
                UUID.randomUUID(), new BigDecimal("10000.00"), PaymentMethod.COD,
                "12 Test Street", "0900000000", null
        );
        cod.transitionTo(OrderStatus.CONFIRMED);
        assertThat(cod.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(cod.getPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);
    }

    @Test
    void expiresOnlyPendingOrdersAtTheirDeadline() {
        ShopOrder order = ShopOrder.create(
                UUID.randomUUID(), new BigDecimal("10000.00"), PaymentMethod.COD,
                "12 Test Street", "0900000000", null
        );
        Instant deadline = Instant.parse("2026-01-02T00:00:00Z");
        order.assignExpiration(deadline);

        assertThat(order.isExpired(deadline.minusSeconds(1))).isFalse();
        assertThat(order.isExpired(deadline)).isTrue();
        order.expire(deadline);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancellationReason()).isEqualTo("Đơn hàng tự động hết hạn");
        assertThat(order.getExpiresAt()).isNull();
    }
}
