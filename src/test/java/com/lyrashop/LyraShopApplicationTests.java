package com.lyrashop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.lyrashop.auth.entity.RefreshSession;
import com.lyrashop.auth.entity.RefreshTokenDigest;
import com.lyrashop.auth.repository.RefreshSessionRepository;
import com.lyrashop.user.entity.User;
import com.lyrashop.user.entity.UserRole;
import com.lyrashop.user.repository.UserRepository;

import jakarta.persistence.EntityManager;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LyraShopApplicationTests {

    private static final DockerImageName MYSQL_IMAGE = DockerImageName.parse(
            "mysql:8.0.46@sha256:7dcddc01f13bab2f15cde676d44d01f61fc9f99fe7785e86196dfc07d358ae2b"
    ).asCompatibleSubstituteFor("mysql");

    private static final String PASSWORD_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    @Container
    @ServiceConnection(name = "mysql")
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>(MYSQL_IMAGE)
            .withDatabaseName("lyrashop_test")
            .withUsername("lyrashop")
            .withPassword("integration-test-password")
            .withCommand(
                    "--character-set-server=utf8mb4",
                    "--collation-server=utf8mb4_0900_ai_ci",
                    "--default-time-zone=+00:00"
            );

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshSessionRepository refreshSessionRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void startsWithValidatedIdentityMigration() {
        var currentMigration = flyway.info().current();

        assertThat(MYSQL.isRunning()).isTrue();
        assertThat(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).isEqualTo(1);
        assertThat(currentMigration).isNotNull();
        assertThat(currentMigration.getVersion()).isEqualTo(MigrationVersion.fromVersion("1"));
        assertThat(currentMigration.getDescription()).isEqualTo("create identity tables");
        assertThat(currentMigration.getState()).isEqualTo(MigrationState.SUCCESS);
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
        assertThat(flyway.migrate().migrationsExecuted).isZero();

        assertThatThrownBy(flyway::clean)
                .isInstanceOf(FlywayException.class)
                .hasMessageContaining("disabled");

        Integer expectedTables = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name IN ('users', 'refresh_sessions', 'flyway_schema_history')
                """,
                Integer.class
        );
        assertThat(expectedTables).isEqualTo(3);
    }

    @Test
    @Transactional
    void persistsIdentityEntitiesWithBinaryUuidsAndTokenDigests() {
        String submittedEmail = " Customer." + UUID.randomUUID() + "@Example.COM ";
        String normalizedEmail = submittedEmail.strip().toLowerCase(Locale.ROOT);
        User user = User.createCustomer(submittedEmail, PASSWORD_HASH, " Test Customer ", " ");

        userRepository.saveAndFlush(user);
        UUID userId = user.getId();
        entityManager.clear();

        User storedUser = userRepository.findByEmail(normalizedEmail).orElseThrow();
        assertThat(storedUser.getId()).isEqualTo(userId);
        assertThat(storedUser.getEmail()).isEqualTo(normalizedEmail);
        assertThat(storedUser.getPasswordHash()).isEqualTo(PASSWORD_HASH);
        assertThat(storedUser.getFullName()).isEqualTo("Test Customer");
        assertThat(storedUser.getPhone()).isNull();
        assertThat(storedUser.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(storedUser.isActive()).isTrue();
        assertThat(storedUser.getVersion()).isZero();
        assertThat(storedUser.getCreatedAt()).isNotNull();
        assertThat(storedUser.getUpdatedAt()).isNotNull();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT OCTET_LENGTH(id) FROM users WHERE email = ?",
                Integer.class,
                normalizedEmail
        )).isEqualTo(16);

        String rawToken = "raw-refresh-token-" + UUID.randomUUID();
        RefreshTokenDigest tokenDigest = RefreshTokenDigest.fromRawToken(rawToken);
        UUID familyId = UUID.randomUUID();
        Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MICROS);
        RefreshSession session = RefreshSession.issue(storedUser, familyId, tokenDigest, expiresAt);

        refreshSessionRepository.saveAndFlush(session);
        UUID sessionId = session.getId();
        entityManager.clear();

        RefreshSession storedSession = refreshSessionRepository.findByTokenDigest(tokenDigest).orElseThrow();
        assertThat(storedSession.getId()).isEqualTo(sessionId);
        assertThat(storedSession.getUser().getId()).isEqualTo(userId);
        assertThat(storedSession.getFamilyId()).isEqualTo(familyId);
        assertThat(storedSession.getTokenHash()).containsExactly(tokenDigest.bytes());
        assertThat(storedSession.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(storedSession.getConsumedAt()).isNull();
        assertThat(storedSession.getRevokedAt()).isNull();
        assertThat(storedSession.getCreatedAt()).isNotNull();
        assertThat(storedSession.getVersion()).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT OCTET_LENGTH(id) FROM refresh_sessions WHERE id = ?",
                Integer.class,
                uuidBytes(sessionId)
        )).isEqualTo(16);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT HEX(token_hash) FROM refresh_sessions WHERE id = ?",
                String.class,
                uuidBytes(sessionId)
        )).isEqualTo(HexFormat.of().formatHex(tokenDigest.bytes()).toUpperCase(Locale.ROOT));
    }

    @Test
    @Transactional
    void rejectsCaseInsensitiveDuplicateEmailsAtDatabaseBoundary() {
        String email = "duplicate-" + UUID.randomUUID() + "@example.com";
        userRepository.saveAndFlush(User.createCustomer(email, PASSWORD_HASH, "First Customer", null));

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO users (id, email, password, full_name) VALUES (?, ?, ?, ?)",
                uuidBytes(UUID.randomUUID()),
                email.toUpperCase(Locale.ROOT),
                PASSWORD_HASH,
                "Second Customer"
        ))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uk_users_email");
    }

    @Test
    @Transactional
    void rejectsUnknownUserRolesAtDatabaseBoundary() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                """
                INSERT INTO users (id, email, password, full_name, role)
                VALUES (?, ?, ?, ?, ?)
                """,
                uuidBytes(UUID.randomUUID()),
                "invalid-role-" + UUID.randomUUID() + "@example.com",
                PASSWORD_HASH,
                "Invalid Role",
                "SUPER_ADMIN"
        ))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("chk_users_role");
    }

    @Test
    void allowsOnlyOneConcurrentRefreshConsumption() throws Exception {
        User user = userRepository.saveAndFlush(User.createCustomer(
                "consume-" + UUID.randomUUID() + "@example.com",
                PASSWORD_HASH,
                "Token Consumer",
                null
        ));
        RefreshTokenDigest tokenDigest = RefreshTokenDigest.fromRawToken(
                "consume-token-" + UUID.randomUUID()
        );
        RefreshSession session = refreshSessionRepository.saveAndFlush(RefreshSession.issue(
                user,
                UUID.randomUUID(),
                tokenDigest,
                Instant.now().plus(7, ChronoUnit.DAYS)
        ));
        UUID sessionId = session.getId();
        CyclicBarrier startBarrier = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            var firstAttempt = executor.submit(() -> consumeAfterBarrier(sessionId, startBarrier));
            var secondAttempt = executor.submit(() -> consumeAfterBarrier(sessionId, startBarrier));

            assertThat(List.of(
                    firstAttempt.get(30, TimeUnit.SECONDS),
                    secondAttempt.get(30, TimeUnit.SECONDS)
            )).containsExactlyInAnyOrder(1, 0);
        } finally {
            executor.shutdownNow();
        }

        assertThat(refreshSessionRepository.findActiveByTokenDigest(tokenDigest)).isEmpty();
        RefreshSession consumedSession = refreshSessionRepository.findByTokenDigest(tokenDigest).orElseThrow();
        assertThat(consumedSession.getConsumedAt()).isNotNull();
        assertThat(consumedSession.getConsumedAt()).isAfterOrEqualTo(consumedSession.getCreatedAt());
        assertThat(consumedSession.getVersion()).isEqualTo(1);
    }

    @Test
    @Transactional
    void rejectsDuplicateTokenDigests() {
        User user = userRepository.saveAndFlush(User.createCustomer(
                "duplicate-token-" + UUID.randomUUID() + "@example.com",
                PASSWORD_HASH,
                "Duplicate Token",
                null
        ));
        RefreshTokenDigest tokenDigest = RefreshTokenDigest.fromRawToken(
                "duplicate-token-" + UUID.randomUUID()
        );
        Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);

        refreshSessionRepository.saveAndFlush(
                RefreshSession.issue(user, UUID.randomUUID(), tokenDigest, expiresAt)
        );

        assertThatThrownBy(() -> refreshSessionRepository.saveAndFlush(
                RefreshSession.issue(user, UUID.randomUUID(), tokenDigest, expiresAt)
        ))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uk_refresh_sessions_token_hash");
    }

    @Test
    @Transactional
    void rejectsSessionsThatExpireBeforeCreation() {
        User user = userRepository.saveAndFlush(User.createCustomer(
                "invalid-expiry-" + UUID.randomUUID() + "@example.com",
                PASSWORD_HASH,
                "Invalid Expiry",
                null
        ));

        assertThatThrownBy(() -> refreshSessionRepository.saveAndFlush(RefreshSession.issue(
                user,
                UUID.randomUUID(),
                RefreshTokenDigest.fromRawToken("invalid-expiry-token-" + UUID.randomUUID()),
                Instant.now().minus(1, ChronoUnit.MINUTES)
        )))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("chk_refresh_sessions_expiry");
    }

    @Test
    @Transactional
    void excludesExpiredAndInactiveUserSessionsFromActiveOperations() {
        User user = userRepository.saveAndFlush(User.createCustomer(
                "inactive-" + UUID.randomUUID() + "@example.com",
                PASSWORD_HASH,
                "Inactive Customer",
                null
        ));
        RefreshTokenDigest expiredDigest = RefreshTokenDigest.fromRawToken(
                "historically-expired-token-" + UUID.randomUUID()
        );
        UUID expiredSessionId = UUID.randomUUID();

        assertThat(jdbcTemplate.update(
                """
                INSERT INTO refresh_sessions (
                    id, user_id, family_id, token_hash, created_at, expires_at
                )
                VALUES (
                    ?, ?, ?, ?,
                    TIMESTAMPADD(DAY, -2, CURRENT_TIMESTAMP(6)),
                    TIMESTAMPADD(DAY, -1, CURRENT_TIMESTAMP(6))
                )
                """,
                uuidBytes(expiredSessionId),
                uuidBytes(user.getId()),
                uuidBytes(UUID.randomUUID()),
                expiredDigest.bytes()
        )).isEqualTo(1);
        assertThat(refreshSessionRepository.findActiveByTokenDigest(expiredDigest)).isEmpty();
        assertThat(refreshSessionRepository.consumeIfActive(expiredSessionId)).isZero();

        RefreshTokenDigest inactiveDigest = RefreshTokenDigest.fromRawToken(
                "inactive-user-token-" + UUID.randomUUID()
        );
        RefreshSession inactiveSession = refreshSessionRepository.saveAndFlush(RefreshSession.issue(
                user,
                UUID.randomUUID(),
                inactiveDigest,
                Instant.now().plus(7, ChronoUnit.DAYS)
        ));
        long initialVersion = user.getVersion();
        Instant initialUpdatedAt = user.getUpdatedAt();

        user.deactivate();
        userRepository.saveAndFlush(user);
        entityManager.clear();

        User inactiveUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(inactiveUser.isActive()).isFalse();
        assertThat(inactiveUser.getVersion()).isEqualTo(initialVersion + 1);
        assertThat(inactiveUser.getUpdatedAt()).isAfterOrEqualTo(initialUpdatedAt);
        assertThat(refreshSessionRepository.findActiveByTokenDigest(inactiveDigest)).isEmpty();
        assertThat(refreshSessionRepository.consumeIfActive(inactiveSession.getId())).isZero();
    }

    @Test
    @Transactional
    void revokesRefreshFamiliesAndAllUserSessionsAtomically() {
        User user = userRepository.saveAndFlush(User.createCustomer(
                "revoke-" + UUID.randomUUID() + "@example.com",
                PASSWORD_HASH,
                "Revoked Customer",
                null
        ));
        UUID familyId = UUID.randomUUID();
        RefreshTokenDigest firstDigest = RefreshTokenDigest.fromRawToken(
                "family-token-1-" + UUID.randomUUID()
        );
        RefreshTokenDigest secondDigest = RefreshTokenDigest.fromRawToken(
                "family-token-2-" + UUID.randomUUID()
        );
        RefreshTokenDigest otherFamilyDigest = RefreshTokenDigest.fromRawToken(
                "other-family-token-" + UUID.randomUUID()
        );
        Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);

        RefreshSession firstFamilySession = refreshSessionRepository.saveAndFlush(
                RefreshSession.issue(user, familyId, firstDigest, expiresAt)
        );
        refreshSessionRepository.saveAndFlush(RefreshSession.issue(user, familyId, secondDigest, expiresAt));
        refreshSessionRepository.saveAndFlush(RefreshSession.issue(
                user,
                UUID.randomUUID(),
                otherFamilyDigest,
                expiresAt
        ));

        assertThat(refreshSessionRepository.revokeFamilyIfActive(familyId)).isEqualTo(2);
        assertThat(refreshSessionRepository.revokeFamilyIfActive(familyId)).isZero();
        assertThat(refreshSessionRepository.findActiveByTokenDigest(firstDigest)).isEmpty();
        assertThat(refreshSessionRepository.findActiveByTokenDigest(secondDigest)).isEmpty();
        assertThat(refreshSessionRepository.consumeIfActive(firstFamilySession.getId())).isZero();
        assertThat(refreshSessionRepository.findActiveByTokenDigest(otherFamilyDigest)).isPresent();

        RefreshSession revokedSession = refreshSessionRepository.findByTokenDigest(firstDigest).orElseThrow();
        assertThat(revokedSession.getRevokedAt()).isNotNull();
        assertThat(revokedSession.getRevokedAt()).isAfterOrEqualTo(revokedSession.getCreatedAt());
        assertThat(revokedSession.getVersion()).isEqualTo(1);

        assertThat(refreshSessionRepository.revokeAllByUserId(user.getId())).isEqualTo(1);
        assertThat(refreshSessionRepository.revokeAllByUserId(user.getId())).isZero();
        assertThat(refreshSessionRepository.findActiveByTokenDigest(otherFamilyDigest)).isEmpty();
    }

    @Test
    @Transactional
    void cascadesSessionsWhenUserIsDeleted() {
        User user = userRepository.saveAndFlush(User.createCustomer(
                "cascade-" + UUID.randomUUID() + "@example.com",
                PASSWORD_HASH,
                "Cascade Customer",
                null
        ));
        byte[] userId = uuidBytes(user.getId());
        RefreshSession validSession = refreshSessionRepository.saveAndFlush(RefreshSession.issue(
                user,
                UUID.randomUUID(),
                RefreshTokenDigest.fromRawToken("cascade-token-" + UUID.randomUUID()),
                Instant.now().plus(7, ChronoUnit.DAYS)
        ));
        byte[] sessionId = uuidBytes(validSession.getId());

        assertThat(jdbcTemplate.update("DELETE FROM users WHERE id = ?", userId)).isEqualTo(1);
        entityManager.clear();

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_sessions WHERE id = ?",
                Integer.class,
                sessionId
        )).isZero();
    }

    private int consumeAfterBarrier(UUID sessionId, CyclicBarrier startBarrier) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        Integer result = transaction.execute(status -> {
            await(startBarrier);
            return refreshSessionRepository.consumeIfActive(sessionId);
        });
        if (result == null) {
            throw new IllegalStateException("consume transaction returned no result");
        }
        return result;
    }

    private static void await(CyclicBarrier barrier) {
        try {
            barrier.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("concurrent consume was interrupted", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("concurrent consume could not start", exception);
        }
    }

    private static byte[] uuidBytes(UUID value) {
        return ByteBuffer.allocate(16)
                .putLong(value.getMostSignificantBits())
                .putLong(value.getLeastSignificantBits())
                .array();
    }
}
