package com.lyrashop.auth.entity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Pattern;

public final class RefreshTokenDigest {

    public static final int LENGTH = 32;
    public static final int RAW_TOKEN_BYTES = 32;
    public static final int RAW_TOKEN_LENGTH = 43;

    private static final Pattern RAW_TOKEN_PATTERN =
            Pattern.compile("[A-Za-z0-9_-]{" + RAW_TOKEN_LENGTH + "}");
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final Base64.Encoder URL_ENCODER =
            Base64.getUrlEncoder().withoutPadding();

    private final byte[] value;

    private RefreshTokenDigest(byte[] value) {
        this.value = Arrays.copyOf(value, value.length);
    }

    public static RefreshTokenDigest fromRawToken(String rawToken) {
        validateRawToken(rawToken);

        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return new RefreshTokenDigest(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required by the Java platform", exception);
        }
    }

    private static void validateRawToken(String rawToken) {
        if (rawToken == null || !RAW_TOKEN_PATTERN.matcher(rawToken).matches()) {
            throw new IllegalArgumentException("rawToken has an invalid format");
        }

        byte[] decoded;
        try {
            decoded = URL_DECODER.decode(rawToken);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("rawToken has an invalid format", exception);
        }

        try {
            if (decoded.length != RAW_TOKEN_BYTES
                    || !URL_ENCODER.encodeToString(decoded).equals(rawToken)) {
                throw new IllegalArgumentException("rawToken has an invalid format");
            }
        } finally {
            Arrays.fill(decoded, (byte) 0);
        }
    }

    public byte[] bytes() {
        return Arrays.copyOf(value, value.length);
    }
}
