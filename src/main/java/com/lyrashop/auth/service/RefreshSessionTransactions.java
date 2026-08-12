package com.lyrashop.auth.service;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
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

@Service
public class RefreshSessionTransactions {

    private static final String EXPIRY_CONSTRAINT = "chk_refresh_sessions_expiry";

    private final RefreshSessionRepository refreshSessionRepository;
    private final UserRepository userRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenProperties properties;
    private final AccessTokenService accessTokenService;

    public RefreshSessionTransactions(
            RefreshSessionRepository refreshSessionRepository,
            UserRepository userRepository,
            RefreshTokenGenerator refreshTokenGenerator,
            RefreshTokenProperties properties,
            AccessTokenService accessTokenService
    ) {
        this.refreshSessionRepository = refreshSessionRepository;
        this.userRepository = userRepository;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.properties = properties;
        this.accessTokenService = accessTokenService;
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.READ_COMMITTED
    )
    public Optional<IssuedAuthentication> issueInitial(UUID userId) {
        Objects.requireNonNull(userId, "userId");
        Optional<User> activeUser = userRepository.findActiveById(userId);
        if (activeUser.isEmpty()) {
            return Optional.empty();
        }

        Instant issuedAt = databaseNow();
        Instant familyExpiresAt = issuedAt.plus(properties.ttl());
        return Optional.of(issue(
                activeUser.orElseThrow(),
                UUID.randomUUID(),
                familyExpiresAt,
                issuedAt
        ));
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.READ_COMMITTED
    )
    public Optional<IssuedAuthentication> rotate(RefreshTokenDigest tokenDigest) {
        Objects.requireNonNull(tokenDigest, "tokenDigest");
        Optional<RefreshSessionSnapshot> storedSession =
                refreshSessionRepository.findSnapshotByTokenDigest(tokenDigest);
        if (storedSession.isEmpty()) {
            return Optional.empty();
        }

        RefreshSessionSnapshot session = storedSession.orElseThrow();
        if (session.consumedAt() != null
                || session.revokedAt() != null
                || !session.userActive()) {
            revokeFamily(session);
            return Optional.empty();
        }

        Instant checkedAt = databaseNow();
        if (!session.expiresAt().isAfter(checkedAt)) {
            return Optional.empty();
        }

        if (refreshSessionRepository.consumeIfActive(session.id()) != 1) {
            revokeFamily(session);
            return Optional.empty();
        }

        Optional<User> activeUser = userRepository.findActiveById(session.userId());
        if (activeUser.isEmpty()) {
            revokeFamily(session);
            return Optional.empty();
        }

        Instant rotatedAt = databaseNow();
        if (!session.expiresAt().isAfter(rotatedAt)) {
            revokeFamily(session);
            return Optional.empty();
        }

        try {
            return Optional.of(issue(
                    activeUser.orElseThrow(),
                    session.familyId(),
                    session.expiresAt(),
                    rotatedAt
            ));
        } catch (DataIntegrityViolationException exception) {
            if (isExpiryConstraintViolation(exception)) {
                throw new InvalidRefreshTokenException(exception);
            }
            throw exception;
        }
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.READ_COMMITTED
    )
    public void logout(UUID authenticatedUserId, RefreshTokenDigest tokenDigest) {
        Objects.requireNonNull(authenticatedUserId, "authenticatedUserId");
        Objects.requireNonNull(tokenDigest, "tokenDigest");

        refreshSessionRepository.findSnapshotByTokenDigest(tokenDigest)
                .filter(session -> authenticatedUserId.equals(session.userId()))
                .ifPresent(this::revokeFamily);
    }

    private IssuedAuthentication issue(
            User user,
            UUID familyId,
            Instant familyExpiresAt,
            Instant issuedAt
    ) {
        String rawToken = refreshTokenGenerator.generate();
        RefreshTokenDigest tokenDigest = RefreshTokenDigest.fromRawToken(rawToken);
        refreshSessionRepository.saveAndFlush(RefreshSession.issue(
                user,
                familyId,
                tokenDigest,
                familyExpiresAt
        ));

        IssuedAccessToken accessToken = accessTokenService.issue(user);
        long expiresInSeconds = Math.max(
                1,
                Duration.between(issuedAt, familyExpiresAt).toSeconds()
        );
        return new IssuedAuthentication(
                accessToken,
                new IssuedRefreshToken(rawToken, familyExpiresAt, expiresInSeconds)
        );
    }

    private Instant databaseNow() {
        return Objects.requireNonNull(
                refreshSessionRepository.currentDatabaseTime(),
                "database time"
        );
    }

    private void revokeFamily(RefreshSessionSnapshot session) {
        refreshSessionRepository.revokeFamilyForUserIfActive(
                session.familyId(),
                session.userId()
        );
    }

    private static boolean isExpiryConstraintViolation(Throwable failure) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable current = failure;
        while (current != null && visited.add(current)) {
            if (current instanceof ConstraintViolationException violation
                    && isExpiryConstraint(violation.getConstraintName())) {
                return true;
            }
            if (current instanceof SQLException sqlException
                    && containsExpiryConstraint(sqlException.getMessage())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static boolean isExpiryConstraint(String constraintName) {
        if (constraintName == null) {
            return false;
        }
        String normalized = constraintName
                .replace("`", "")
                .replace(String.valueOf((char) 34), "")
                .toLowerCase(Locale.ROOT);
        return normalized.equals(EXPIRY_CONSTRAINT)
                || normalized.endsWith("." + EXPIRY_CONSTRAINT);
    }

    private static boolean containsExpiryConstraint(String message) {
        return message != null
                && message.toLowerCase(Locale.ROOT).contains(EXPIRY_CONSTRAINT);
    }
}
