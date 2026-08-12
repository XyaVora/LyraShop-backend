package com.lyrashop.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.lyrashop.config.AuthProtectionProperties;
import com.lyrashop.exception.AuthenticationCapacityExceededException;

class BoundedPasswordOperationsTests {

    private static final AuthProtectionProperties SINGLE_OPERATION_CAPACITY =
            new AuthProtectionProperties(8_192, 8_192, 1, 3);

    @Test
    void sharesCapacityBetweenHashingAndPasswordVerification() throws Exception {
        CountDownLatch hashingStarted = new CountDownLatch(1);
        CountDownLatch releaseHash = new CountDownLatch(1);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> {
            hashingStarted.countDown();
            if (!releaseHash.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("test hash was not released");
            }
            return "encoded-" + invocation.getArgument(0, String.class);
        });
        when(passwordEncoder.matches("third-password", "stored-hash")).thenReturn(true);
        BoundedPasswordOperations operations =
                new BoundedPasswordOperations(passwordEncoder, SINGLE_OPERATION_CAPACITY);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            var firstHash = executor.submit(() -> operations.hash("first-password"));
            assertThat(hashingStarted.await(5, TimeUnit.SECONDS)).isTrue();

            assertCapacityExceeded(
                    () -> operations.matches("second-password", "stored-hash")
            );

            releaseHash.countDown();
            assertThat(firstHash.get(5, TimeUnit.SECONDS)).isEqualTo("encoded-first-password");
            assertThat(operations.matches("third-password", "stored-hash")).isTrue();
        } finally {
            releaseHash.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void matchingAlsoBlocksHashingAndReleasesThePermit() throws Exception {
        CountDownLatch matchingStarted = new CountDownLatch(1);
        CountDownLatch releaseMatch = new CountDownLatch(1);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.matches(anyString(), anyString())).thenAnswer(invocation -> {
            matchingStarted.countDown();
            if (!releaseMatch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("test match was not released");
            }
            return true;
        });
        when(passwordEncoder.encode("after-release")).thenReturn("encoded-after-release");
        BoundedPasswordOperations operations =
                new BoundedPasswordOperations(passwordEncoder, SINGLE_OPERATION_CAPACITY);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            var firstMatch =
                    executor.submit(() -> operations.matches("candidate", "stored-hash"));
            assertThat(matchingStarted.await(5, TimeUnit.SECONDS)).isTrue();

            assertCapacityExceeded(() -> operations.hash("blocked-password"));

            releaseMatch.countDown();
            assertThat(firstMatch.get(5, TimeUnit.SECONDS)).isTrue();
            assertThat(operations.hash("after-release")).isEqualTo("encoded-after-release");
        } finally {
            releaseMatch.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void releasesCapacityWhenPasswordVerificationFails() {
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("invalid stored hash"))
                .thenReturn(false);
        BoundedPasswordOperations operations =
                new BoundedPasswordOperations(passwordEncoder, SINGLE_OPERATION_CAPACITY);

        assertThatThrownBy(() -> operations.matches("candidate", "invalid-hash"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("invalid stored hash");
        assertThat(operations.matches("candidate", "valid-hash")).isFalse();
    }

    private static void assertCapacityExceeded(Runnable operation) {
        assertThatThrownBy(operation::run)
                .isInstanceOfSatisfying(
                        AuthenticationCapacityExceededException.class,
                        exception -> assertThat(exception.getRetryAfterSeconds()).isEqualTo(3)
                );
    }
}
