package com.lyrashop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Validated
@ConfigurationProperties(prefix = "app.security.auth")
public record AuthProtectionProperties(
        @Min(1024) @Max(65_536) int maxRequestBodyBytes,
        @Min(1024) @Max(65_536) int businessRequestBodyBytes,
        @Min(1) @Max(64) int maxConcurrentPasswordHashes,
        @Min(1) @Max(60) int retryAfterSeconds
) {
}
