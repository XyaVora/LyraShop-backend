package com.lyrashop.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.dao.TransientDataAccessResourceException;

import com.lyrashop.auth.entity.RefreshTokenDigest;
import com.lyrashop.exception.InvalidCredentialsException;
import com.lyrashop.exception.InvalidRefreshTokenException;

class RefreshTokenServiceTests {

    private static final String RAW_TOKEN = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(new byte[RefreshTokenDigest.RAW_TOKEN_BYTES]);

    private final RefreshSessionTransactions transactions =
            mock(RefreshSessionTransactions.class);
    private final RefreshTokenService service = new RefreshTokenService(transactions);

    @Test
    void returnsRotatedAuthenticationAfterOneTransactionalInvocation() {
        IssuedAuthentication expected = authentication();
        when(transactions.rotate(any(RefreshTokenDigest.class)))
                .thenReturn(Optional.of(expected));

        assertThat(service.refresh(RAW_TOKEN)).isSameAs(expected);

        verify(transactions).rotate(any(RefreshTokenDigest.class));
    }

    @Test
    void mapsMalformedAndUnknownTokensToTheSameGenericFailure() {
        assertThatThrownBy(() -> service.refresh("not-a-refresh-token"))
                .isInstanceOf(InvalidRefreshTokenException.class);
        verifyNoInteractions(transactions);

        when(transactions.rotate(any(RefreshTokenDigest.class)))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.refresh(RAW_TOKEN))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void retriesEachTransientFailureWithANewTransactionalInvocation() {
        IssuedAuthentication expected = authentication();
        when(transactions.rotate(any(RefreshTokenDigest.class)))
                .thenThrow(new TransientDataAccessResourceException("deadlock"))
                .thenReturn(Optional.of(expected));

        assertThat(service.refresh(RAW_TOKEN)).isSameAs(expected);

        verify(transactions, times(2)).rotate(any(RefreshTokenDigest.class));
    }

    @Test
    void stopsAfterTheBoundedNumberOfTransientRetries() {
        TransientDataAccessResourceException failure =
                new TransientDataAccessResourceException("database unavailable");
        when(transactions.rotate(any(RefreshTokenDigest.class))).thenThrow(failure);

        assertThatThrownBy(() -> service.refresh(RAW_TOKEN)).isSameAs(failure);

        verify(transactions, times(RefreshTokenService.MAX_TRANSACTION_ATTEMPTS))
                .rotate(any(RefreshTokenDigest.class));
    }

    @Test
    void initialIssuanceRejectionStaysAGenericCredentialsFailure() {
        UUID userId = UUID.randomUUID();
        when(transactions.issueInitial(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.issueInitial(userId))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void malformedLogoutIsIdempotentAndDoesNotReachPersistence() {
        service.logout(UUID.randomUUID(), "malformed");

        verify(transactions, never()).logout(any(), any());
    }

    @Test
    void logoutRetriesTransientFailures() {
        UUID userId = UUID.randomUUID();
        org.mockito.Mockito.doThrow(
                new TransientDataAccessResourceException("deadlock")
        ).doNothing().when(transactions).logout(
                org.mockito.ArgumentMatchers.eq(userId),
                any(RefreshTokenDigest.class)
        );

        service.logout(userId, RAW_TOKEN);

        verify(transactions, times(2)).logout(
                org.mockito.ArgumentMatchers.eq(userId),
                any(RefreshTokenDigest.class)
        );
    }

    private static IssuedAuthentication authentication() {
        return new IssuedAuthentication(
                new IssuedAccessToken("access-token", 900),
                new IssuedRefreshToken(
                        RAW_TOKEN,
                        Instant.parse("2030-01-08T00:00:00Z"),
                        604800
                )
        );
    }
}
