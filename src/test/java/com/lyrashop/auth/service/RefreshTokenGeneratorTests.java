package com.lyrashop.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.lyrashop.auth.entity.RefreshTokenDigest;

class RefreshTokenGeneratorTests {

    private final RefreshTokenGenerator generator = new RefreshTokenGenerator();

    @Test
    void generatesUniqueCanonicalBase64UrlTokensWith256BitsOfEntropy() {
        Set<String> tokens = new HashSet<>();

        for (int index = 0; index < 128; index++) {
            String token = generator.generate();

            assertThat(token)
                    .hasSize(RefreshTokenDigest.RAW_TOKEN_LENGTH)
                    .matches("[A-Za-z0-9_-]{43}");
            assertThat(Base64.getUrlDecoder().decode(token))
                    .hasSize(RefreshTokenDigest.RAW_TOKEN_BYTES);
            assertThatCode(() -> RefreshTokenDigest.fromRawToken(token))
                    .doesNotThrowAnyException();
            tokens.add(token);
        }

        assertThat(tokens).hasSize(128);
    }

    @Test
    void rejectsMalformedAndNonCanonicalTokenMaterialBeforeDigesting() {
        String canonical = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(new byte[RefreshTokenDigest.RAW_TOKEN_BYTES]);
        String nonCanonical = canonical.substring(0, canonical.length() - 1) + "B";

        assertInvalid(null);
        assertInvalid("");
        assertInvalid("a".repeat(42));
        assertInvalid("a".repeat(44));
        assertInvalid(canonical + "=");
        assertInvalid(canonical.substring(0, 42) + "+");
        assertInvalid(nonCanonical);
    }

    @Test
    void digestIsDeterministicAndDefensivelyCopied() {
        String token = generator.generate();
        byte[] first = RefreshTokenDigest.fromRawToken(token).bytes();
        byte[] second = RefreshTokenDigest.fromRawToken(token).bytes();

        assertThat(first).containsExactly(second);
        first[0] ^= 0xff;
        assertThat(RefreshTokenDigest.fromRawToken(token).bytes())
                .containsExactly(second);
    }

    private static void assertInvalid(String rawToken) {
        assertThatThrownBy(() -> RefreshTokenDigest.fromRawToken(rawToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("rawToken has an invalid format");
    }
}
