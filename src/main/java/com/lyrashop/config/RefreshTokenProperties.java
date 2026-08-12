package com.lyrashop.config;

import java.time.Duration;

import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "app.security.refresh-token")
public record RefreshTokenProperties(
        @NotNull
        @DurationMin(days = 1)
        @DurationMax(days = 7)
        Duration ttl
) {
}
