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

class BoundedPasswordHasherTests {

    private static final AuthProtectionProperties SINGLE_HASH_CAPACITY =
            new AuthProtectionProperties(8_192, 1, 3);

    @Test
    void rejectsImmediatelyWhenHashingCapacityIsExhausted() throws Exception {
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
        BoundedPasswordHasher hasher =
                new BoundedPasswordHasher(passwordEncoder, SINGLE_HASH_CAPACITY);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            var firstHash = executor.submit(() -> hasher.hash("first-password"));
            assertThat(hashingStarted.await(5, TimeUnit.SECONDS)).isTrue();

            assertThatThrownBy(() -> hasher.hash("second-password"))
                    .isInstanceOfSatisfying(
                            AuthenticationCapacityExceededException.class,
                            exception -> assertThat(exception.getRetryAfterSeconds()).isEqualTo(3)
                    );

            releaseHash.countDown();
            assertThat(firstHash.get(5, TimeUnit.SECONDS)).isEqualTo("encoded-first-password");
            assertThat(hasher.hash("third-password")).isEqualTo("encoded-third-password");
        } finally {
            releaseHash.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void releasesCapacityWhenTheEncoderFails() {
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(anyString()))
                .thenThrow(new IllegalStateException("encoder failure"))
                .thenReturn("encoded-after-failure");
        BoundedPasswordHasher hasher =
                new BoundedPasswordHasher(passwordEncoder, SINGLE_HASH_CAPACITY);

        assertThatThrownBy(() -> hasher.hash("failing-password"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("encoder failure");
        assertThat(hasher.hash("next-password")).isEqualTo("encoded-after-failure");
    }
}
