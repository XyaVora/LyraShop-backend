package com.lyrashop.security;

import java.util.concurrent.Semaphore;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.lyrashop.config.AuthProtectionProperties;
import com.lyrashop.exception.AuthenticationCapacityExceededException;

@Component
public class BoundedPasswordHasher {

    private final PasswordEncoder passwordEncoder;
    private final Semaphore capacity;
    private final int retryAfterSeconds;

    public BoundedPasswordHasher(
            PasswordEncoder passwordEncoder,
            AuthProtectionProperties properties
    ) {
        this.passwordEncoder = passwordEncoder;
        this.capacity = new Semaphore(properties.maxConcurrentPasswordHashes());
        this.retryAfterSeconds = properties.retryAfterSeconds();
    }

    public String hash(String rawPassword) {
        if (!capacity.tryAcquire()) {
            throw new AuthenticationCapacityExceededException(retryAfterSeconds);
        }

        try {
            return passwordEncoder.encode(rawPassword);
        } finally {
            capacity.release();
        }
    }
}
