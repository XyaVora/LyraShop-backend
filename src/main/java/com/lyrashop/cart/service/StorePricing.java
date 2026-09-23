package com.lyrashop.cart.service;

import java.math.BigDecimal;

public final class StorePricing {
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("500000.00");
    private static final BigDecimal STANDARD_SHIPPING_FEE = new BigDecimal("30000.00");
    private static final BigDecimal GIFT_WRAP_FEE = new BigDecimal("30000.00");

    private StorePricing() {}

    public static BigDecimal shippingFee(BigDecimal merchandiseTotal) {
        if (merchandiseTotal == null || merchandiseTotal.signum() == 0
                || merchandiseTotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return STANDARD_SHIPPING_FEE;
    }

    public static BigDecimal freeShippingThreshold() { return FREE_SHIPPING_THRESHOLD; }
    public static BigDecimal standardShippingFee() { return STANDARD_SHIPPING_FEE; }
    public static BigDecimal giftWrapFee() { return GIFT_WRAP_FEE; }
}
