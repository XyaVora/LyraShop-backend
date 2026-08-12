package com.lyrashop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lyrashop.auth.controller.AuthController;
import com.lyrashop.auth.entity.RefreshSession;
import com.lyrashop.auth.entity.RefreshTokenDigest;
import com.lyrashop.auth.repository.RefreshSessionRepository;
import com.lyrashop.auth.service.RefreshCookieService;
import com.lyrashop.auth.service.IssuedAuthentication;
import com.lyrashop.auth.service.RefreshTokenService;
import com.lyrashop.config.SecurityConfig;
import com.lyrashop.exception.InvalidRefreshTokenException;
import com.lyrashop.security.AccessTokenClaimsValidator;
import com.lyrashop.user.entity.User;
import com.lyrashop.user.entity.UserRole;
import com.lyrashop.user.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;

@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "app.security.cors.allowed-origins=https://shop.example.test",
                "app.security.jwt.secret-base64="
                        + "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
        }
)
@AutoConfigureMockMvc
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
    private RefreshTokenService refreshTokenService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void registersAnonymousCustomerWithoutChangingPasswordMaterial() throws Exception {
        String submittedEmail = " Registration." + UUID.randomUUID() + "@Example.COM ";
        String canonicalEmail = submittedEmail.strip().toLowerCase(Locale.ROOT);
        String rawPassword = "  correct horse battery staple  ";

        var result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson(
                                submittedEmail,
                                rawPassword,
                                "  Registration Customer  ",
                                " 0901234567 "
                        )))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email").value(canonicalEmail))
                .andExpect(jsonPath("$.fullName").value("Registration Customer"))
                .andExpect(jsonPath("$.phone").value("0901234567"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(cookie().doesNotExist("JSESSIONID"))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE))
                .andReturn();

        User storedUser = userRepository.findByEmail(canonicalEmail).orElseThrow();
        assertThat(storedUser.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(storedUser.isActive()).isTrue();
        assertThat(storedUser.getPasswordHash()).isNotEqualTo(rawPassword);
        assertThat(storedUser.getPasswordHash()).startsWith("{bcrypt}$2b$12$");
        assertThat(passwordEncoder.matches(rawPassword, storedUser.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches(rawPassword.strip(), storedUser.getPasswordHash())).isFalse();
        assertThat(result.getResponse().getContentAsString())
                .doesNotContain(rawPassword, storedUser.getPasswordHash(), "passwordHash");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_sessions WHERE user_id = ?",
                Integer.class,
                uuidBytes(storedUser.getId())
        )).isZero();
    }

    @Test
    void returnsSafeFieldErrorsForInvalidRegistration() throws Exception {
        String rejectedPassword = "short";

        var result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson(
                                "not-an-email",
                                rejectedPassword,
                                " ",
                                "1".repeat(21)
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists())
                .andExpect(jsonPath("$.fieldErrors.fullName").exists())
                .andExpect(jsonPath("$.fieldErrors.phone").exists())
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .doesNotContain(rejectedPassword, "not-an-email", "1".repeat(21));
    }

    @Test
    void enforcesBcryptUtf8BoundaryBeforePersistence() throws Exception {
        String acceptedEmail = "utf8-accepted-" + UUID.randomUUID() + "@example.com";
        String threeByteCharacter = String.valueOf((char) 0x20ac);
        String acceptedPassword = threeByteCharacter.repeat(24);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson(
                                acceptedEmail,
                                acceptedPassword,
                                "UTF-8 Accepted",
                                null
                        )))
                .andExpect(status().isCreated());

        User acceptedUser = userRepository.findByEmail(acceptedEmail).orElseThrow();
        assertThat(passwordEncoder.matches(acceptedPassword, acceptedUser.getPasswordHash())).isTrue();

        String rejectedEmail = "utf8-rejected-" + UUID.randomUUID() + "@example.com";
        String rejectedPassword = threeByteCharacter.repeat(25);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson(
                                rejectedEmail,
                                rejectedPassword,
                                "UTF-8 Rejected",
                                null
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.password").exists());

        assertThat(userRepository.findByEmail(rejectedEmail)).isEmpty();
    }

    @Test
    void rejectsPrivilegeFieldsOutsideTheRegistrationContract() throws Exception {
        String email = "mass-assignment-" + UUID.randomUUID() + "@example.com";
        Map<String, Object> request = registrationPayload(
                email,
                "valid password for customer",
                "Mass Assignment",
                null
        );
        request.put("role", "ADMIN");
        request.put("active", false);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));

        assertThat(userRepository.findByEmail(email)).isEmpty();
    }

    @Test
    void returnsConflictForCanonicalDuplicateEmailWithoutLeakingDatabaseDetails() throws Exception {
        String email = "duplicate-registration-" + UUID.randomUUID() + "@example.com";
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson(
                                email,
                                "first valid customer password",
                                "First Registration",
                                null
                        )))
                .andExpect(status().isCreated());

        var duplicate = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson(
                                " " + email.toUpperCase(Locale.ROOT) + " ",
                                "second valid customer password",
                                "Second Registration",
                                null
                        )))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"))
                .andReturn();

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?",
                Integer.class,
                email
        )).isEqualTo(1);
        assertThat(duplicate.getResponse().getContentAsString())
                .doesNotContain("uk_users_email", "Duplicate entry", "DataIntegrityViolationException");
    }

    @Test
    void allowsOnlyOneConcurrentRegistrationForCanonicalEmail() throws Exception {
        String email = "concurrent-registration-" + UUID.randomUUID() + "@example.com";
        String firstRequest = registrationJson(
                email,
                "first concurrent password",
                "Concurrent First",
                null
        );
        String secondRequest = registrationJson(
                email.toUpperCase(Locale.ROOT),
                "second concurrent password",
                "Concurrent Second",
                null
        );
        CyclicBarrier startBarrier = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            var firstAttempt = executor.submit(() -> registerAfterBarrier(firstRequest, startBarrier));
            var secondAttempt = executor.submit(() -> registerAfterBarrier(secondRequest, startBarrier));

            assertThat(List.of(
                    firstAttempt.get(30, TimeUnit.SECONDS),
                    secondAttempt.get(30, TimeUnit.SECONDS)
            )).containsExactlyInAnyOrder(201, 409);
        } finally {
            executor.shutdownNow();
        }

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?",
                Integer.class,
                email
        )).isEqualTo(1);
    }

    @Test
    void logsInAnActiveCustomerAndReturnsOnlyAValidatedAccessToken() throws Exception {
        String submittedEmail = " Login." + UUID.randomUUID() + "@Example.COM ";
        String canonicalEmail = submittedEmail.strip().toLowerCase(Locale.ROOT);
        String rawPassword = "  login password stays opaque  ";
        User user = userRepository.saveAndFlush(User.createCustomer(
                canonicalEmail,
                passwordEncoder.encode(rawPassword),
                "Login Customer",
                null
        ));

        var result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(submittedEmail, rawPassword)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(header().string(HttpHeaders.PRAGMA, "no-cache"))
                .andExpect(header().string(AuthController.XSRF_HEADER_NAME, not(emptyString())))
                .andExpect(cookie().exists(RefreshCookieService.COOKIE_NAME))
                .andExpect(cookie().httpOnly(RefreshCookieService.COOKIE_NAME, true))
                .andExpect(cookie().secure(RefreshCookieService.COOKIE_NAME, true))
                .andExpect(cookie().path(
                        RefreshCookieService.COOKIE_NAME,
                        RefreshCookieService.COOKIE_PATH
                ))
                .andExpect(cookie().maxAge(RefreshCookieService.COOKIE_NAME, 604_800))
                .andExpect(cookie().exists(SecurityConfig.XSRF_COOKIE_NAME))
                .andExpect(cookie().httpOnly(SecurityConfig.XSRF_COOKIE_NAME, true))
                .andExpect(cookie().secure(SecurityConfig.XSRF_COOKIE_NAME, true))
                .andExpect(cookie().doesNotExist("JSESSIONID"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

        var response = objectMapper.readTree(result.getResponse().getContentAsByteArray());
        String accessToken = response.path("accessToken").asText();
        var decoded = jwtDecoder.decode(accessToken);
        assertThat(decoded.getSubject()).isEqualTo(user.getId().toString());
        assertThat(decoded.getClaimAsString(
                AccessTokenClaimsValidator.TOKEN_USE_CLAIM
        )).isEqualTo(AccessTokenClaimsValidator.ACCESS_TOKEN_USE);
        assertThat(decoded.getClaimAsStringList(
                AccessTokenClaimsValidator.ROLES_CLAIM
        )).containsExactly(UserRole.CUSTOMER.name());
        assertThat(decoded.getClaims()).doesNotContainKeys(
                "email",
                "fullName",
                "phone",
                "password",
                "passwordHash"
        );
        assertThat(result.getResponse().getContentAsString())
                .doesNotContain(
                        rawPassword,
                        user.getPasswordHash(),
                        canonicalEmail,
                        result.getResponse()
                                .getCookie(RefreshCookieService.COOKIE_NAME)
                                .getValue()
                );
        assertThat(result.getResponse()
                .getCookie(RefreshCookieService.COOKIE_NAME)
                .getAttribute("SameSite")).isEqualTo("Strict");
        assertThat(result.getResponse()
                .getCookie(SecurityConfig.XSRF_COOKIE_NAME)
                .getAttribute("SameSite")).isEqualTo("Strict");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_sessions WHERE user_id = ?",
                Integer.class,
                uuidBytes(user.getId())
        )).isOne();
        String rawRefreshToken = result.getResponse()
                .getCookie(RefreshCookieService.COOKIE_NAME)
                .getValue();
        assertThat(refreshSessionRepository.findByTokenDigest(
                RefreshTokenDigest.fromRawToken(rawRefreshToken)
        )).isPresent();
    }

    @Test
    void protectsRefreshWithCsrfRotatesTokensAndRevokesTheFamilyOnReplay() throws Exception {
        String email = "refresh-flow-" + UUID.randomUUID() + "@example.com";
        String password = "refresh flow password";
        User user = userRepository.saveAndFlush(User.createCustomer(
                email,
                passwordEncoder.encode(password),
                "Refresh Customer",
                null
        ));

        var login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        Cookie originalRefresh = login.getResponse().getCookie(RefreshCookieService.COOKIE_NAME);
        Cookie csrfCookie = login.getResponse().getCookie(SecurityConfig.XSRF_COOKIE_NAME);
        String csrfToken = login.getResponse().getHeader(AuthController.XSRF_HEADER_NAME);
        assertThat(originalRefresh).isNotNull();
        assertThat(csrfCookie).isNotNull();
        assertThat(csrfToken).isNotBlank();

        mockMvc.perform(get("/api/v1/auth/csrf").cookie(csrfCookie))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(header().string(AuthController.XSRF_HEADER_NAME, csrfToken));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(originalRefresh, csrfCookie))
                .andExpect(status().isForbidden())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.code").value("CSRF_REQUIRED"));

        RefreshSession originalSession = refreshSessionRepository.findByTokenDigest(
                RefreshTokenDigest.fromRawToken(originalRefresh.getValue())
        ).orElseThrow();
        assertThat(originalSession.getConsumedAt()).isNull();

        var refresh = mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(originalRefresh, csrfCookie)
                        .header(AuthController.XSRF_HEADER_NAME, csrfToken))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(header().string(AuthController.XSRF_HEADER_NAME, csrfToken))
                .andExpect(cookie().exists(RefreshCookieService.COOKIE_NAME))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();
        Cookie rotatedRefresh = refresh.getResponse().getCookie(RefreshCookieService.COOKIE_NAME);
        assertThat(rotatedRefresh.getValue()).isNotEqualTo(originalRefresh.getValue());

        entityManager.clear();
        RefreshSession consumedSession = refreshSessionRepository.findByTokenDigest(
                RefreshTokenDigest.fromRawToken(originalRefresh.getValue())
        ).orElseThrow();
        RefreshSession successor = refreshSessionRepository.findByTokenDigest(
                RefreshTokenDigest.fromRawToken(rotatedRefresh.getValue())
        ).orElseThrow();
        assertThat(consumedSession.getConsumedAt()).isNotNull();
        assertThat(successor.getFamilyId()).isEqualTo(consumedSession.getFamilyId());
        assertThat(successor.getExpiresAt()).isEqualTo(consumedSession.getExpiresAt());
        assertThat(successor.getUser().getId()).isEqualTo(user.getId());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(originalRefresh, csrfCookie)
                        .header(AuthController.XSRF_HEADER_NAME, csrfToken))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist(HttpHeaders.WWW_AUTHENTICATE))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(cookie().maxAge(RefreshCookieService.COOKIE_NAME, 0))
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(rotatedRefresh, csrfCookie)
                        .header(AuthController.XSRF_HEADER_NAME, csrfToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));

        entityManager.clear();
        assertThat(refreshSessionRepository.findByTokenDigest(
                RefreshTokenDigest.fromRawToken(rotatedRefresh.getValue())
        ).orElseThrow().getRevokedAt()).isNotNull();
    }

    @Test
    void logsOutTheAuthenticatedRefreshFamilyAndClearsAuthenticationCookies() throws Exception {
        String email = "logout-flow-" + UUID.randomUUID() + "@example.com";
        String password = "logout flow password";
        userRepository.saveAndFlush(User.createCustomer(
                email,
                passwordEncoder.encode(password),
                "Logout Customer",
                null
        ));

        var login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken = objectMapper.readTree(login.getResponse().getContentAsByteArray())
                .path("accessToken")
                .asText();
        Cookie refreshCookie = login.getResponse().getCookie(RefreshCookieService.COOKIE_NAME);
        Cookie csrfCookie = login.getResponse().getCookie(SecurityConfig.XSRF_COOKIE_NAME);
        String csrfToken = login.getResponse().getHeader(AuthController.XSRF_HEADER_NAME);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .cookie(refreshCookie, csrfCookie)
                        .header(AuthController.XSRF_HEADER_NAME, csrfToken))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(cookie().maxAge(RefreshCookieService.COOKIE_NAME, 0))
                .andExpect(cookie().maxAge(SecurityConfig.XSRF_COOKIE_NAME, 0));

        assertThatThrownBy(() -> refreshTokenService.refresh(refreshCookie.getValue()))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void concurrentRefreshReplayRevokesTheWinningSuccessor() throws Exception {
        String email = "concurrent-refresh-" + UUID.randomUUID() + "@example.com";
        String password = "concurrent refresh password";
        userRepository.saveAndFlush(User.createCustomer(
                email,
                passwordEncoder.encode(password),
                "Concurrent Refresh Customer",
                null
        ));
        var login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        String rawToken = login.getResponse()
                .getCookie(RefreshCookieService.COOKIE_NAME)
                .getValue();
        RefreshSession original = refreshSessionRepository.findByTokenDigest(
                RefreshTokenDigest.fromRawToken(rawToken)
        ).orElseThrow();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2);
        try {
            Future<IssuedAuthentication> first =
                    executor.submit(() -> rotateAfterBarrier(rawToken, barrier));
            Future<IssuedAuthentication> second =
                    executor.submit(() -> rotateAfterBarrier(rawToken, barrier));
            List<IssuedAuthentication> results = Arrays.asList(
                    first.get(20, TimeUnit.SECONDS),
                    second.get(20, TimeUnit.SECONDS)
            );

            assertThat(results).filteredOn(result -> result != null).hasSize(1);
            IssuedAuthentication winner = results.stream()
                    .filter(result -> result != null)
                    .findFirst()
                    .orElseThrow();
            entityManager.clear();
            assertThat(refreshSessionRepository.findByTokenDigest(
                    RefreshTokenDigest.fromRawToken(winner.refreshToken().value())
            ).orElseThrow().getRevokedAt()).isNotNull();
            assertThat(jdbcTemplate.queryForObject(
                    """
                            SELECT COUNT(*)
                            FROM refresh_sessions
                            WHERE family_id = ?
                              AND consumed_at IS NULL
                              AND revoked_at IS NULL
                              AND expires_at > UTC_TIMESTAMP(6)
                            """,
                    Integer.class,
                    uuidBytes(original.getFamilyId())
            )).isZero();
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    void returnsTheSameSafeFailureForUnknownWrongAndInactiveCredentials() throws Exception {
        String rawPassword = "generic login password";
        String activeEmail = "active-login-" + UUID.randomUUID() + "@example.com";
        String inactiveEmail = "inactive-login-" + UUID.randomUUID() + "@example.com";
        userRepository.saveAndFlush(User.createCustomer(
                activeEmail,
                passwordEncoder.encode(rawPassword),
                "Active Login",
                null
        ));
        User inactiveUser = userRepository.saveAndFlush(User.createCustomer(
                inactiveEmail,
                passwordEncoder.encode(rawPassword),
                "Inactive Login",
                null
        ));
        inactiveUser.deactivate();
        userRepository.saveAndFlush(inactiveUser);

        List<Map.Entry<String, String>> attempts = List.of(
                Map.entry("missing-" + UUID.randomUUID() + "@example.com", rawPassword),
                Map.entry(activeEmail, "wrong login password"),
                Map.entry(inactiveEmail, rawPassword)
        );
        ObjectNode expectedProblemContract = null;
        for (Map.Entry<String, String> attempt : attempts) {
            var result = mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson(attempt.getKey(), attempt.getValue())))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_PROBLEM_JSON
                    ))
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(header().string(HttpHeaders.PRAGMA, "no-cache"))
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                    .andExpect(jsonPath("$.message").value("Invalid email or password"))
                    .andExpect(jsonPath("$.path").value("/api/v1/auth/login"))
                    .andExpect(jsonPath("$.fieldErrors").isMap())
                    .andExpect(jsonPath("$.accessToken").doesNotExist())
                    .andReturn();

            assertThat(result.getResponse().getContentAsString())
                    .doesNotContain(
                            attempt.getKey(),
                            attempt.getValue(),
                            inactiveUser.getPasswordHash()
                    );
            ObjectNode problem = (ObjectNode) objectMapper.readTree(
                    result.getResponse().getContentAsByteArray()
            );
            problem.remove("timestamp");
            if (expectedProblemContract == null) {
                expectedProblemContract = problem;
            } else {
                assertThat(problem).isEqualTo(expectedProblemContract);
            }
        }
    }

    @Test
    void rejectsUnsafeLoginBodiesBeforeAuthentication() throws Exception {
        Map<String, Object> unexpectedField = new LinkedHashMap<>();
        unexpectedField.put("email", "login@example.com");
        unexpectedField.put("password", "valid login password");
        unexpectedField.put("role", "ADMIN");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unexpectedField)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));

        String oversizedPassword = "x".repeat(8_300);
        var oversized = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("oversized-login@example.com", oversizedPassword)))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.code").value("PAYLOAD_TOO_LARGE"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/login"))
                .andReturn();

        assertThat(oversized.getResponse().getContentAsString())
                .doesNotContain(oversizedPassword, "oversized-login@example.com");
    }

    @Test
    void authenticatesValidBearerTokensWhileKeepingUnknownRoutesDenied() throws Exception {
        String email = "bearer-login-" + UUID.randomUUID() + "@example.com";
        String password = "bearer token password";
        userRepository.saveAndFlush(User.createCustomer(
                email,
                passwordEncoder.encode(password),
                "Bearer Customer",
                null
        ));
        var login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken = objectMapper.readTree(login.getResponse().getContentAsByteArray())
                .path("accessToken")
                .asText();

        mockMvc.perform(get("/api/v1/private"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        mockMvc.perform(get("/api/v1/private")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        mockMvc.perform(get("/api/v1/private")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void keepsRegistrationPublicAndAllOtherRoutesFailClosed() throws Exception {
        mockMvc.perform(get("/api/v1/auth/register"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(header().doesNotExist(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"));

        mockMvc.perform(post("/api/v1/private")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CSRF_REQUIRED"));

        mockMvc.perform(post("/api/v1/private")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void appliesExactOriginCorsAllowlist() throws Exception {
        mockMvc.perform(options("/api/v1/auth/register")
                        .header(HttpHeaders.ORIGIN, "https://shop.example.test")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.POST.name())
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, HttpHeaders.CONTENT_TYPE))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "https://shop.example.test"
                ))
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                        containsStringIgnoringCase(HttpHeaders.CONTENT_TYPE)
                ))
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
                        "true"
                ));

        mockMvc.perform(options("/api/v1/auth/refresh")
                        .header(HttpHeaders.ORIGIN, "https://shop.example.test")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.POST.name())
                        .header(
                                HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
                                AuthController.XSRF_HEADER_NAME
                        ))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "https://shop.example.test"
                ))
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                        containsStringIgnoringCase(AuthController.XSRF_HEADER_NAME)
                ))
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
                        "true"
                ));

        mockMvc.perform(options("/api/v1/auth/register")
                        .header(HttpHeaders.ORIGIN, "https://attacker.example.test")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.POST.name()))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void returnsSafeErrorsForMalformedAndUnsupportedBodies() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("not-json"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
    }

    @Test
    void rejectsOversizedRegistrationBeforeJsonParsingAndPersistence() throws Exception {
        String email = "oversized-registration-" + UUID.randomUUID() + "@example.com";
        String rejectedPassword = "x".repeat(8_300);

        var result = mockMvc.perform(post("/api/v1/auth/register")
                        .header(HttpHeaders.ORIGIN, "https://shop.example.test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson(
                                email,
                                rejectedPassword,
                                "Oversized Registration",
                                null
                        )))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "https://shop.example.test"
                ))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.code").value("PAYLOAD_TOO_LARGE"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/register"))
                .andReturn();

        assertThat(userRepository.findByEmail(email)).isEmpty();
        assertThat(result.getResponse().getContentAsString())
                .doesNotContain(email, rejectedPassword);
    }

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

        String rawToken = rawRefreshToken();
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
        RefreshTokenDigest tokenDigest = RefreshTokenDigest.fromRawToken(rawRefreshToken());
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
        RefreshTokenDigest tokenDigest = RefreshTokenDigest.fromRawToken(rawRefreshToken());
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
                RefreshTokenDigest.fromRawToken(rawRefreshToken()),
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
        RefreshTokenDigest expiredDigest = RefreshTokenDigest.fromRawToken(rawRefreshToken());
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

        RefreshTokenDigest inactiveDigest = RefreshTokenDigest.fromRawToken(rawRefreshToken());
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
        RefreshTokenDigest firstDigest = RefreshTokenDigest.fromRawToken(rawRefreshToken());
        RefreshTokenDigest secondDigest = RefreshTokenDigest.fromRawToken(rawRefreshToken());
        RefreshTokenDigest otherFamilyDigest =
                RefreshTokenDigest.fromRawToken(rawRefreshToken());
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
                RefreshTokenDigest.fromRawToken(rawRefreshToken()),
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

    private String registrationJson(
            String email,
            String password,
            String fullName,
            String phone
    ) throws Exception {
        return objectMapper.writeValueAsString(registrationPayload(email, password, fullName, phone));
    }

    private String loginJson(String email, String password) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", password
        ));
    }

    private static Map<String, Object> registrationPayload(
            String email,
            String password,
            String fullName,
            String phone
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("email", email);
        payload.put("password", password);
        payload.put("fullName", fullName);
        payload.put("phone", phone);
        return payload;
    }

    private int registerAfterBarrier(String requestBody, CyclicBarrier startBarrier) {
        await(startBarrier);
        try {
            return mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andReturn()
                    .getResponse()
                    .getStatus();
        } catch (Exception exception) {
            throw new IllegalStateException("concurrent registration request failed", exception);
        }
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

    private IssuedAuthentication rotateAfterBarrier(
            String rawToken,
            CyclicBarrier startBarrier
    ) {
        await(startBarrier);
        try {
            return refreshTokenService.refresh(rawToken);
        } catch (InvalidRefreshTokenException exception) {
            return null;
        }
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

    private static String rawRefreshToken() {
        ByteBuffer entropy = ByteBuffer.allocate(32)
                .putLong(UUID.randomUUID().getMostSignificantBits())
                .putLong(UUID.randomUUID().getLeastSignificantBits())
                .putLong(UUID.randomUUID().getMostSignificantBits())
                .putLong(UUID.randomUUID().getLeastSignificantBits());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(entropy.array());
    }
}
