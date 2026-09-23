package com.lyrashop.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.security.email-verification")
public record EmailVerificationProperties(
        String frontendUrl,
        String fromAddress,
        Duration tokenTtl,
        Duration requestCooldown
) {
    public EmailVerificationProperties {
        if (frontendUrl == null || frontendUrl.isBlank()) throw new IllegalArgumentException("email verification frontend URL is required");
        if (fromAddress == null || fromAddress.isBlank()) throw new IllegalArgumentException("email verification from address is required");
        if (tokenTtl == null || tokenTtl.isZero() || tokenTtl.isNegative()) throw new IllegalArgumentException("email verification TTL must be positive");
        if (requestCooldown == null || requestCooldown.isZero() || requestCooldown.isNegative()) throw new IllegalArgumentException("email verification request cooldown must be positive");
    }
}
