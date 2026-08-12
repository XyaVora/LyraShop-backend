package com.lyrashop.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.lyrashop.auth.entity.RefreshSession;
import com.lyrashop.auth.entity.RefreshTokenDigest;
import com.lyrashop.auth.repository.RefreshSessionRepository;
import com.lyrashop.auth.repository.RefreshSessionSnapshot;
import com.lyrashop.config.RefreshTokenProperties;
import com.lyrashop.exception.InvalidRefreshTokenException;
import com.lyrashop.user.entity.User;
import com.lyrashop.user.repository.UserRepository;

class RefreshSessionTransactionsTests {

    private static final String RAW_TOKEN = encodedToken((byte) 0);
    private static final String ISSUED_TOKEN = encodedToken((byte) 1);
    private static final Instant INITIAL_TIME = Instant.parse("2030-01-01T00:00:00Z");
    private static final Instant FAMILY_EXPIRY = Instant.parse("2030-01-08T00:00:00Z");

    private final RefreshSessionRepository refreshSessionRepository =
            mock(RefreshSessionRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final RefreshTokenGenerator refreshTokenGenerator =
            mock(RefreshTokenGenerator.class);
    private final AccessTokenService accessTokenService = mock(AccessTokenService.class);
    private final RefreshSessionTransactions transactions = new RefreshSessionTransactions(
            refreshSessionRepository,
            userRepository,
            refreshTokenGenerator,
            new RefreshTokenProperties(Duration.ofDays(7)),
            accessTokenService
    );

    @Test
    void initialIssuanceRefetchesActiveUserAndUsesDatabaseTimeForAbsoluteExpiry() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        IssuedAccessToken accessToken = new IssuedAccessToken("access-token", 900);
        when(userRepository.findActiveById(userId)).thenReturn(Optional.of(user));
        when(refreshSessionRepository.currentDatabaseTime()).thenReturn(INITIAL_TIME);
        when(refreshTokenGenerator.generate()).thenReturn(ISSUED_TOKEN);
        when(accessTokenService.issue(user)).thenReturn(accessToken);

        IssuedAuthentication result = transactions.issueInitial(userId).orElseThrow();

        assertThat(result.accessToken()).isSameAs(accessToken);
        assertThat(result.refreshToken().value()).isEqualTo(ISSUED_TOKEN);
        assertThat(result.refreshToken().expiresAt()).isEqualTo(FAMILY_EXPIRY);
        assertThat(result.refreshToken().expiresInSeconds()).isEqualTo(604800);

        ArgumentCaptor<RefreshSession> sessionCaptor =
                ArgumentCaptor.forClass(RefreshSession.class);
        verify(refreshSessionRepository).saveAndFlush(sessionCaptor.capture());
        RefreshSession session = sessionCaptor.getValue();
        assertThat(session.getUser()).isSameAs(user);
        assertThat(session.getFamilyId()).isNotNull();
        assertThat(session.getExpiresAt()).isEqualTo(FAMILY_EXPIRY);
        assertThat(session.getTokenHash())
                .containsExactly(RefreshTokenDigest.fromRawToken(ISSUED_TOKEN).bytes());
    }

    @Test
    void initialIssuanceRejectsAUserThatIsNoLongerActive() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findActiveById(userId)).thenReturn(Optional.empty());

        assertThat(transactions.issueInitial(userId)).isEmpty();

        verifyNoInteractions(refreshTokenGenerator, accessTokenService);
        verify(refreshSessionRepository, never()).saveAndFlush(any());
    }

    @Test
    void rotationUsesConsumeCasAsAuthorityAndPreservesFamilyExpiry() {
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        Instant rotatedAt = Instant.parse("2030-01-02T00:00:00Z");
        RefreshTokenDigest digest = RefreshTokenDigest.fromRawToken(RAW_TOKEN);
        User user = activeUser(userId);
        RefreshSessionSnapshot snapshot = activeSnapshot(
                sessionId,
                userId,
                familyId,
                FAMILY_EXPIRY
        );
        when(refreshSessionRepository.findSnapshotByTokenDigest(digest))
                .thenReturn(Optional.of(snapshot));
        when(refreshSessionRepository.currentDatabaseTime())
                .thenReturn(INITIAL_TIME, rotatedAt);
        when(refreshSessionRepository.consumeIfActive(sessionId)).thenReturn(1);
        when(userRepository.findActiveById(userId)).thenReturn(Optional.of(user));
        when(refreshTokenGenerator.generate()).thenReturn(ISSUED_TOKEN);
        IssuedAccessToken accessToken = new IssuedAccessToken("rotated-access", 900);
        when(accessTokenService.issue(user)).thenReturn(accessToken);

        IssuedAuthentication result = transactions.rotate(digest).orElseThrow();

        assertThat(result.refreshToken().expiresAt()).isEqualTo(FAMILY_EXPIRY);
        assertThat(result.refreshToken().expiresInSeconds()).isEqualTo(518400);
        ArgumentCaptor<RefreshSession> sessionCaptor =
                ArgumentCaptor.forClass(RefreshSession.class);
        verify(refreshSessionRepository).saveAndFlush(sessionCaptor.capture());
        assertThat(sessionCaptor.getValue().getFamilyId()).isEqualTo(familyId);
        assertThat(sessionCaptor.getValue().getExpiresAt()).isEqualTo(FAMILY_EXPIRY);
    }

    @Test
    void expiryConstraintRaceBecomesAGenericRefreshFailureAndRollsBack() {
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        RefreshTokenDigest digest = RefreshTokenDigest.fromRawToken(RAW_TOKEN);
        User user = activeUser(userId);
        when(refreshSessionRepository.findSnapshotByTokenDigest(digest))
                .thenReturn(Optional.of(activeSnapshot(
                        sessionId,
                        userId,
                        UUID.randomUUID(),
                        FAMILY_EXPIRY
                )));
        when(refreshSessionRepository.currentDatabaseTime())
                .thenReturn(INITIAL_TIME, INITIAL_TIME.plusSeconds(1));
        when(refreshSessionRepository.consumeIfActive(sessionId)).thenReturn(1);
        when(userRepository.findActiveById(userId)).thenReturn(Optional.of(user));
        when(refreshTokenGenerator.generate()).thenReturn(ISSUED_TOKEN);
        when(refreshSessionRepository.saveAndFlush(any())).thenThrow(
                new DataIntegrityViolationException(
                        "refresh expiry constraint",
                        new SQLException(
                                "Check constraint 'chk_refresh_sessions_expiry' is violated",
                                "HY000",
                                3819
                        )
                )
        );

        assertThatThrownBy(() -> transactions.rotate(digest))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("Refresh token is invalid");
        verify(accessTokenService, never()).issue(any());
    }

    @Test
    void consumedTokenRevokesItsWholeOwnedFamilyBeforeReturningEmpty() {
        UUID userId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        RefreshTokenDigest digest = RefreshTokenDigest.fromRawToken(RAW_TOKEN);
        RefreshSessionSnapshot consumed = new RefreshSessionSnapshot(
                UUID.randomUUID(),
                userId,
                familyId,
                FAMILY_EXPIRY,
                INITIAL_TIME,
                null,
                true
        );
        when(refreshSessionRepository.findSnapshotByTokenDigest(digest))
                .thenReturn(Optional.of(consumed));

        assertThat(transactions.rotate(digest)).isEmpty();

        verify(refreshSessionRepository).revokeFamilyForUserIfActive(familyId, userId);
        verify(refreshSessionRepository, never()).consumeIfActive(any());
        verifyNoInteractions(refreshTokenGenerator, accessTokenService);
    }

    @Test
    void losingTheConsumeRaceRevokesTheFamily() {
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        RefreshTokenDigest digest = RefreshTokenDigest.fromRawToken(RAW_TOKEN);
        when(refreshSessionRepository.findSnapshotByTokenDigest(digest))
                .thenReturn(Optional.of(activeSnapshot(
                        sessionId,
                        userId,
                        familyId,
                        FAMILY_EXPIRY
                )));
        when(refreshSessionRepository.currentDatabaseTime()).thenReturn(INITIAL_TIME);
        when(refreshSessionRepository.consumeIfActive(sessionId)).thenReturn(0);

        assertThat(transactions.rotate(digest)).isEmpty();

        verify(refreshSessionRepository).revokeFamilyForUserIfActive(familyId, userId);
        verify(refreshSessionRepository, never()).saveAndFlush(any());
        verifyNoInteractions(refreshTokenGenerator, accessTokenService);
    }

    @Test
    void expiredTokenDoesNotAttemptConsumptionOrCreateASuccessor() {
        UUID sessionId = UUID.randomUUID();
        RefreshTokenDigest digest = RefreshTokenDigest.fromRawToken(RAW_TOKEN);
        when(refreshSessionRepository.findSnapshotByTokenDigest(digest))
                .thenReturn(Optional.of(activeSnapshot(
                        sessionId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        INITIAL_TIME
                )));
        when(refreshSessionRepository.currentDatabaseTime()).thenReturn(INITIAL_TIME);

        assertThat(transactions.rotate(digest)).isEmpty();

        verify(refreshSessionRepository, never()).consumeIfActive(sessionId);
        verify(refreshSessionRepository, never()).saveAndFlush(any());
    }

    @Test
    void logoutRevokesOnlyWhenTheCookieSessionBelongsToTheBearerUser() {
        UUID authenticatedUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        RefreshTokenDigest digest = RefreshTokenDigest.fromRawToken(RAW_TOKEN);
        when(refreshSessionRepository.findSnapshotByTokenDigest(digest))
                .thenReturn(Optional.of(activeSnapshot(
                        UUID.randomUUID(),
                        otherUserId,
                        familyId,
                        FAMILY_EXPIRY
                )));

        transactions.logout(authenticatedUserId, digest);
        verify(refreshSessionRepository, never())
                .revokeFamilyForUserIfActive(any(), any());

        when(refreshSessionRepository.findSnapshotByTokenDigest(digest))
                .thenReturn(Optional.of(activeSnapshot(
                        UUID.randomUUID(),
                        authenticatedUserId,
                        familyId,
                        FAMILY_EXPIRY
                )));
        transactions.logout(authenticatedUserId, digest);

        verify(refreshSessionRepository)
                .revokeFamilyForUserIfActive(familyId, authenticatedUserId);
    }

    @Test
    void everyMutationUsesANewReadCommittedTransaction() throws Exception {
        assertTransaction("issueInitial", UUID.class);
        assertTransaction("rotate", RefreshTokenDigest.class);
        assertTransaction("logout", UUID.class, RefreshTokenDigest.class);
    }

    private static void assertTransaction(String methodName, Class<?>... parameters)
            throws Exception {
        Method method = RefreshSessionTransactions.class
                .getDeclaredMethod(methodName, parameters);
        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.propagation()).isEqualTo(Propagation.REQUIRES_NEW);
        assertThat(transactional.isolation()).isEqualTo(Isolation.READ_COMMITTED);
    }

    private static RefreshSessionSnapshot activeSnapshot(
            UUID sessionId,
            UUID userId,
            UUID familyId,
            Instant expiresAt
    ) {
        return new RefreshSessionSnapshot(
                sessionId,
                userId,
                familyId,
                expiresAt,
                null,
                null,
                true
        );
    }

    private static User activeUser(UUID userId) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.isActive()).thenReturn(true);
        return user;
    }

    private static String encodedToken(byte value) {
        byte[] entropy = new byte[RefreshTokenDigest.RAW_TOKEN_BYTES];
        Arrays.fill(entropy, value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(entropy);
    }
}
