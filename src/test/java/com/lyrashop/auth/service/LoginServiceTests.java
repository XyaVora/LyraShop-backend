package com.lyrashop.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import com.lyrashop.auth.dto.LoginRequest;
import com.lyrashop.exception.AuthenticationCapacityExceededException;
import com.lyrashop.exception.InvalidCredentialsException;
import com.lyrashop.security.BoundedPasswordOperations;
import com.lyrashop.security.DummyPasswordHash;
import com.lyrashop.user.entity.User;
import com.lyrashop.user.repository.UserRepository;

class LoginServiceTests {

    private static final String RAW_PASSWORD = "  valid customer password  ";
    private static final String STORED_HASH = "{bcrypt}$2b$12$stored";
    private static final String DUMMY_HASH = "{bcrypt}$2b$12$dummy";
    private static final String SAFE_INVALID_PASSWORD = "invalid-login-password";

    private final UserRepository userRepository = mock(UserRepository.class);
    private final BoundedPasswordOperations passwordOperations =
            mock(BoundedPasswordOperations.class);
    private final DummyPasswordHash dummyPasswordHash = mock(DummyPasswordHash.class);
    private final RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
    private final LoginService loginService = new LoginService(
            userRepository,
            passwordOperations,
            dummyPasswordHash,
            refreshTokenService
    );

    @Test
    void canonicalizesEmailWithoutChangingPasswordMaterial() {
        User user = activeUser();
        IssuedAuthentication issuedAuthentication = issuedAuthentication();
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(passwordOperations.matches(RAW_PASSWORD, STORED_HASH)).thenReturn(true);
        when(refreshTokenService.issueInitial(user.getId())).thenReturn(issuedAuthentication);

        IssuedAuthentication result = loginService.login(new LoginRequest(
                " CUSTOMER@EXAMPLE.COM ",
                RAW_PASSWORD
        ));

        assertThat(result).isSameAs(issuedAuthentication);
        verify(passwordOperations).matches(RAW_PASSWORD, STORED_HASH);
        verifyNoMoreInteractions(passwordOperations);
        verify(refreshTokenService).issueInitial(user.getId());
    }

    @Test
    void unknownAccountPerformsOneDummyComparisonAndReturnsGenericFailure() {
        when(dummyPasswordHash.value()).thenReturn(DUMMY_HASH);
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        when(passwordOperations.matches(RAW_PASSWORD, DUMMY_HASH)).thenReturn(false);

        assertInvalidCredentials(() -> loginService.login(
                new LoginRequest("missing@example.com", RAW_PASSWORD)
        ));

        verify(passwordOperations).matches(RAW_PASSWORD, DUMMY_HASH);
        verifyNoMoreInteractions(passwordOperations);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void wrongPasswordReturnsTheSameGenericFailure() {
        User user = activeUser();
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(passwordOperations.matches(RAW_PASSWORD, STORED_HASH)).thenReturn(false);

        assertInvalidCredentials(() -> loginService.login(
                new LoginRequest("customer@example.com", RAW_PASSWORD)
        ));

        verify(passwordOperations).matches(RAW_PASSWORD, STORED_HASH);
        verifyNoMoreInteractions(passwordOperations);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void inactiveAccountStillPerformsOneRealComparisonAndReturnsGenericFailure() {
        User user = mock(User.class);
        when(user.getPasswordHash()).thenReturn(STORED_HASH);
        when(user.isActive()).thenReturn(false);
        when(userRepository.findByEmail("inactive@example.com")).thenReturn(Optional.of(user));
        when(passwordOperations.matches(RAW_PASSWORD, STORED_HASH)).thenReturn(true);

        assertInvalidCredentials(() -> loginService.login(
                new LoginRequest("inactive@example.com", RAW_PASSWORD)
        ));

        verify(passwordOperations).matches(RAW_PASSWORD, STORED_HASH);
        verifyNoMoreInteractions(passwordOperations);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void rejectsOversizedPasswordAfterOneBoundedComparison() {
        User user = activeUser();
        String oversizedPassword = "x".repeat(73);
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(passwordOperations.matches(SAFE_INVALID_PASSWORD, STORED_HASH)).thenReturn(false);

        assertInvalidCredentials(() -> loginService.login(
                new LoginRequest("customer@example.com", oversizedPassword)
        ));

        verify(passwordOperations).matches(SAFE_INVALID_PASSWORD, STORED_HASH);
        verifyNoMoreInteractions(passwordOperations);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void invalidCanonicalEmailDoesNotReachPersistenceButStillConsumesOneComparison() {
        when(dummyPasswordHash.value()).thenReturn(DUMMY_HASH);
        when(passwordOperations.matches(SAFE_INVALID_PASSWORD, DUMMY_HASH)).thenReturn(false);

        assertInvalidCredentials(() -> loginService.login(
                new LoginRequest(" ", RAW_PASSWORD)
        ));

        verifyNoInteractions(userRepository, refreshTokenService);
        verify(passwordOperations).matches(SAFE_INVALID_PASSWORD, DUMMY_HASH);
        verifyNoMoreInteractions(passwordOperations);
    }

    @Test
    void corruptStoredHashIsReportedAsGenericCredentialsFailure() {
        User user = activeUser();
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(passwordOperations.matches(RAW_PASSWORD, STORED_HASH))
                .thenThrow(new IllegalArgumentException("invalid encoded password"));

        assertInvalidCredentials(() -> loginService.login(
                new LoginRequest("customer@example.com", RAW_PASSWORD)
        ));

        verify(passwordOperations).matches(RAW_PASSWORD, STORED_HASH);
        verifyNoMoreInteractions(passwordOperations);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void propagatesCapacityExhaustionWithoutAccessingTokenService() {
        when(dummyPasswordHash.value()).thenReturn(DUMMY_HASH);
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        AuthenticationCapacityExceededException capacityFailure =
                new AuthenticationCapacityExceededException(3);
        when(passwordOperations.matches(RAW_PASSWORD, DUMMY_HASH)).thenThrow(capacityFailure);

        assertThatThrownBy(() -> loginService.login(
                new LoginRequest("missing@example.com", RAW_PASSWORD)
        )).isSameAs(capacityFailure);

        verify(passwordOperations).matches(RAW_PASSWORD, DUMMY_HASH);
        verifyNoMoreInteractions(passwordOperations);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void keepsPasswordVerificationOutsideATransactionBoundary() throws Exception {
        var loginMethod = LoginService.class.getDeclaredMethod("login", LoginRequest.class);

        assertThat(LoginService.class.isAnnotationPresent(Transactional.class)).isFalse();
        assertThat(loginMethod.isAnnotationPresent(Transactional.class)).isFalse();
    }

    private static User activeUser() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(user.getPasswordHash()).thenReturn(STORED_HASH);
        when(user.isActive()).thenReturn(true);
        return user;
    }

    private static IssuedAuthentication issuedAuthentication() {
        return new IssuedAuthentication(
                new IssuedAccessToken("access-token", 900),
                new IssuedRefreshToken(
                        "A".repeat(43),
                        java.time.Instant.parse("2030-01-08T00:00:00Z"),
                        604800
                )
        );
    }

    private static void assertInvalidCredentials(Runnable login) {
        assertThatThrownBy(login::run).isInstanceOf(InvalidCredentialsException.class);
    }
}
