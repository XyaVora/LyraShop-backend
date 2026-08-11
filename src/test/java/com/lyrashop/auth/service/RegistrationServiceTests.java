package com.lyrashop.auth.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.lyrashop.auth.dto.RegisterRequest;
import com.lyrashop.exception.EmailAlreadyRegisteredException;
import com.lyrashop.user.entity.User;
import com.lyrashop.user.repository.UserRepository;

class RegistrationServiceTests {

    private static final RegisterRequest REQUEST = new RegisterRequest(
            "customer@example.com",
            "valid customer password",
            "Test Customer",
            null
    );

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final RegistrationService registrationService =
            new RegistrationService(userRepository, passwordEncoder);

    @Test
    void mapsOnlyTheEmailUniqueConstraintToAConflict() {
        DataIntegrityViolationException databaseFailure = databaseFailure(
                ConstraintViolationException.ConstraintKind.UNIQUE,
                "uk_users_email"
        );
        when(passwordEncoder.encode(REQUEST.password())).thenReturn("encoded-password");
        when(userRepository.existsByEmail(REQUEST.email())).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(databaseFailure);

        assertThatThrownBy(() -> registrationService.register(REQUEST))
                .isInstanceOf(EmailAlreadyRegisteredException.class)
                .hasCause(databaseFailure);
    }

    @Test
    void rethrowsUnrelatedIntegrityFailures() {
        DataIntegrityViolationException databaseFailure = databaseFailure(
                ConstraintViolationException.ConstraintKind.OTHER,
                "chk_users_role"
        );
        when(passwordEncoder.encode(REQUEST.password())).thenReturn("encoded-password");
        when(userRepository.existsByEmail(REQUEST.email())).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(databaseFailure);

        assertThatThrownBy(() -> registrationService.register(REQUEST))
                .isSameAs(databaseFailure);
    }

    private static DataIntegrityViolationException databaseFailure(
            ConstraintViolationException.ConstraintKind kind,
            String constraintName
    ) {
        SQLException sqlException = new SQLException(
                "Database constraint violation",
                "23000",
                1062
        );
        ConstraintViolationException hibernateException = new ConstraintViolationException(
                "Could not execute statement",
                sqlException,
                kind,
                constraintName
        );
        return new DataIntegrityViolationException("Could not persist user", hibernateException);
    }
}
