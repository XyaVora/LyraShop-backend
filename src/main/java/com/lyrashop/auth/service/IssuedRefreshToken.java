package com.lyrashop.auth.service;

import java.time.Instant;
import java.util.Objects;

public record IssuedRefreshToken(
        String value,
        Instant expiresAt,
        long expiresInSeconds
) {

    public IssuedRefreshToken {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(expiresAt, "expiresAt");
        if (expiresInSeconds < 1) {
            throw new IllegalArgumentException("expiresInSeconds must be positive");
        }
    }

    @Override
    public String toString() {
        return "IssuedRefreshToken[value=[REDACTED], expiresAt=" + expiresAt
                + ", expiresInSeconds=" + expiresInSeconds + "]";
    }
}
