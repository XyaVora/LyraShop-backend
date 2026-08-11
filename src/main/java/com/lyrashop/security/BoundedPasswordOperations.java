package com.lyrashop.security;

import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.lyrashop.config.AuthProtectionProperties;
import com.lyrashop.exception.AuthenticationCapacityExceededException;

@Component
public class BoundedPasswordOperations {

    private final PasswordEncoder passwordEncoder;
    private final Semaphore capacity;
    private final int retryAfterSeconds;

    public BoundedPasswordOperations(
            PasswordEncoder passwordEncoder,
            AuthProtectionProperties properties
    ) {
        this.passwordEncoder = passwordEncoder;
        this.capacity = new Semaphore(properties.maxConcurrentPasswordHashes());
        this.retryAfterSeconds = properties.retryAfterSeconds();
    }

    public String hash(String rawPassword) {
        return withCapacity(() -> passwordEncoder.encode(rawPassword));
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return withCapacity(() -> passwordEncoder.matches(rawPassword, encodedPassword));
    }

    private <T> T withCapacity(Supplier<T> operation) {
        if (!capacity.tryAcquire()) {
            throw new AuthenticationCapacityExceededException(retryAfterSeconds);
        }
        try {
            return operation.get();
        } finally {
            capacity.release();
        }
    }
}
