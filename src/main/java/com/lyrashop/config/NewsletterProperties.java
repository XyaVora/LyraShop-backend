package com.lyrashop.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.newsletter")
public record NewsletterProperties(String frontendUrl, String fromAddress, Duration confirmationTtl) {
    public NewsletterProperties {
        if (frontendUrl == null || frontendUrl.isBlank()) throw new IllegalArgumentException("newsletter frontend URL is required");
        if (fromAddress == null || fromAddress.isBlank()) throw new IllegalArgumentException("newsletter from address is required");
        if (confirmationTtl == null || confirmationTtl.isZero() || confirmationTtl.isNegative()) {
            throw new IllegalArgumentException("newsletter confirmation TTL must be positive");
        }
    }
}
