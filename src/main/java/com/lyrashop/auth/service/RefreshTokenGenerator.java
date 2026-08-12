package com.lyrashop.auth.service;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.lyrashop.auth.entity.RefreshTokenDigest;

@Component
public class RefreshTokenGenerator {

    private final SecureRandom secureRandom;

    public RefreshTokenGenerator() {
        this(new SecureRandom());
    }

    RefreshTokenGenerator(SecureRandom secureRandom) {
        this.secureRandom = Objects.requireNonNull(secureRandom, "secureRandom");
    }

    public String generate() {
        byte[] entropy = new byte[RefreshTokenDigest.RAW_TOKEN_BYTES];
        try {
            secureRandom.nextBytes(entropy);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(entropy);
        } finally {
            Arrays.fill(entropy, (byte) 0);
        }
    }
}
