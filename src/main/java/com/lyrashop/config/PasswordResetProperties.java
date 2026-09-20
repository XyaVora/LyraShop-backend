package com.lyrashop.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.security.password-reset")
public record PasswordResetProperties(String frontendUrl, String fromAddress, Duration tokenTtl) {
    public PasswordResetProperties {
        if (frontendUrl == null || frontendUrl.isBlank()) throw new IllegalArgumentException("password reset frontend URL is required");
        if (fromAddress == null || fromAddress.isBlank()) throw new IllegalArgumentException("password reset from address is required");
        if (tokenTtl == null || tokenTtl.isNegative() || tokenTtl.isZero()) throw new IllegalArgumentException("password reset TTL must be positive");
    }
}
