package com.lyrashop.auth.entity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public final class RefreshTokenDigest {

    public static final int LENGTH = 32;

    private final byte[] value;

    private RefreshTokenDigest(byte[] value) {
        this.value = Arrays.copyOf(value, value.length);
    }

    public static RefreshTokenDigest fromRawToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("rawToken must not be blank");
        }

        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return new RefreshTokenDigest(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required by the Java platform", exception);
        }
    }

    public byte[] bytes() {
        return Arrays.copyOf(value, value.length);
    }
}
