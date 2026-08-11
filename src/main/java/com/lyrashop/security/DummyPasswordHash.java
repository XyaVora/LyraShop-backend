package com.lyrashop.security;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DummyPasswordHash {

    private final String value;

    public DummyPasswordHash(PasswordEncoder passwordEncoder) {
        this.value = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    public String value() {
        return value;
    }
}
