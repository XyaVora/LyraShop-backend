package com.lyrashop.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.order")
public record OrderProperties(
        Duration onlinePaymentTtl,
        Duration confirmationTtl
) {
    public OrderProperties {
        if (onlinePaymentTtl == null || onlinePaymentTtl.isZero() || onlinePaymentTtl.isNegative()) {
            throw new IllegalArgumentException("online payment TTL must be positive");
        }
        if (confirmationTtl == null || confirmationTtl.isZero() || confirmationTtl.isNegative()) {
            throw new IllegalArgumentException("order confirmation TTL must be positive");
        }
    }
}
