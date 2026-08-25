package com.lyrashop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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
import com.lyrashop.auth.service.AccessTokenService;
import com.lyrashop.auth.service.RefreshCookieService;
import com.lyrashop.auth.service.IssuedAuthentication;
import com.lyrashop.auth.service.RefreshTokenService;
import com.lyrashop.catalog.category.entity.Category;
import com.lyrashop.catalog.category.repository.CategoryRepository;
import com.lyrashop.catalog.product.entity.Product;
import com.lyrashop.catalog.product.repository.ProductRepository;
import com.lyrashop.catalog.variant.entity.ProductVariant;
import com.lyrashop.catalog.variant.repository.ProductVariantRepository;
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
    private AccessTokenService accessTokenService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

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
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

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
    void exposesOnlyActiveCategoriesToAnonymousClients() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category active = categoryRepository.saveAndFlush(Category.create(
                "Active Category " + suffix,
                "active-" + suffix,
                "Visible category",
                null
        ));
        Category inactive = categoryRepository.saveAndFlush(Category.create(
                "Inactive Category " + suffix,
                "inactive-" + suffix,
                "Hidden category",
                null
        ));
        inactive.deactivate();
        categoryRepository.saveAndFlush(inactive);

        var list = mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();
        assertThat(list.getResponse().getContentAsString())
                .contains(active.getSlug())
                .doesNotContain(inactive.getSlug());

        mockMvc.perform(get("/api/v1/categories/{id}", active.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(active.getId()))
                .andExpect(jsonPath("$.name").value(active.getName()))
                .andExpect(jsonPath("$.slug").value(active.getSlug()))
                .andExpect(jsonPath("$.description").value("Visible category"))
                .andExpect(jsonPath("$.active").doesNotExist());

        mockMvc.perform(get("/api/v1/categories/{id}", inactive.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"))
                .andExpect(jsonPath("$.path").value(
                        "/api/v1/categories/" + inactive.getId()
                ));
    }

    @Test
    void enforcesCategoryAdminAuthorizationWithoutBusinessCsrf() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String requestBody = categoryJson(
                "Authorized Category " + suffix,
                "authorized-" + suffix,
                "Created by an administrator",
                null
        );

        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        mockMvc.perform(post("/api/v1/admin/categories")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + accessTokenForRole(UserRole.CUSTOMER)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        var created = mockMvc.perform(post("/api/v1/admin/categories")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + accessTokenForRole(UserRole.ADMIN)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Authorized Category " + suffix))
                .andExpect(jsonPath("$.slug").value("authorized-" + suffix))
                .andExpect(jsonPath("$.active").doesNotExist())
                .andReturn();

        long categoryId = objectMapper.readTree(created.getResponse().getContentAsByteArray())
                .path("id")
                .asLong();
        assertThat(categoryRepository.findByIdAndActiveTrue(categoryId)).isPresent();

        mockMvc.perform(get("/api/v1/admin/not-implemented")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + accessTokenForRole(UserRole.ADMIN)
                        ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void validatesCategoryInputAndMapsSlugConflictsSafely() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String adminToken = accessTokenForRole(UserRole.ADMIN);
        String slug = "duplicate-" + suffix;

        mockMvc.perform(post("/api/v1/admin/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson(
                                "First Category",
                                slug,
                                null,
                                null
                        )))
                .andExpect(status().isCreated());

        var duplicate = mockMvc.perform(post("/api/v1/admin/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson(
                                "Duplicate Category",
                                slug.toUpperCase(Locale.ROOT),
                                null,
                                null
                        )))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CATEGORY_SLUG_ALREADY_EXISTS"))
                .andReturn();
        assertThat(duplicate.getResponse().getContentAsString())
                .doesNotContain("uk_categories_slug", "Duplicate entry");

        mockMvc.perform(post("/api/v1/admin/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Unknown Field",
                                  "slug": "unknown-field",
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));

        mockMvc.perform(post("/api/v1/admin/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson(
                                "Missing Parent",
                                "missing-parent-" + suffix,
                                null,
                                Long.MAX_VALUE
                        )))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    void updatesCategoryForAdminsWithoutReactivating() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category parent = categoryRepository.saveAndFlush(Category.create(
                "Parent Category " + suffix, "parent-category-" + suffix, null, null
        ));
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Original Category " + suffix, "original-category-" + suffix, "Before", null
        ));
        String path = "/api/v1/admin/categories/" + category.getId();
        String body = categoryJson(
                "Renamed Category " + suffix,
                "renamed-category-" + suffix,
                "After",
                parent.getId()
        );
        String adminToken = accessTokenForRole(UserRole.ADMIN);

        mockMvc.perform(put(path).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenForRole(UserRole.CUSTOMER))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(put(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace("}", ",\"sku\":\"MASS-ASSIGN\"}")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed Category " + suffix))
                .andExpect(jsonPath("$.slug").value("renamed-category-" + suffix))
                .andExpect(jsonPath("$.parentId").value(parent.getId()))
                .andExpect(jsonPath("$.active").doesNotExist());

        category.deactivate();
        categoryRepository.saveAndFlush(category);
        mockMvc.perform(put(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson(
                                "Still Inactive " + suffix,
                                "still-inactive-" + suffix,
                                null,
                                null
                        )))
                .andExpect(status().isOk());
        entityManager.clear();
        assertThat(categoryRepository.findById(category.getId()).orElseThrow().isActive()).isFalse();
        mockMvc.perform(get("/api/v1/categories/{id}", category.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
        mockMvc.perform(put("/api/v1/admin/categories/{id}", category.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson("Self Parent", "self-parent-" + suffix, null, category.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    void deactivatesCategoriesForAdminsAndHidesThemIdempotently() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Hide Category " + suffix, "hide-category-" + suffix, null, null
        ));
        String path = "/api/v1/admin/categories/" + category.getId() + "/deactivate";
        String adminToken = accessTokenForRole(UserRole.ADMIN);

        mockMvc.perform(patch(path))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenForRole(UserRole.CUSTOMER)))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));
        entityManager.clear();
        assertThat(categoryRepository.findById(category.getId()).orElseThrow().isActive()).isFalse();
        mockMvc.perform(get("/api/v1/categories/{id}", category.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
        mockMvc.perform(patch(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent());
        mockMvc.perform(patch("/api/v1/admin/categories/not-a-number/deactivate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
        mockMvc.perform(patch("/api/v1/admin/categories/0/deactivate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    void mapsConcurrentCategorySlugRaceToOneCreatedAndOneConflict() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String slug = "concurrent-category-" + suffix;
        String requestBody = categoryJson(
                "Concurrent Category",
                slug,
                null,
                null
        );
        String adminToken = accessTokenForRole(UserRole.ADMIN);
        CyclicBarrier startBarrier = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Integer> first = executor.submit(() -> createCategoryAfterBarrier(
                    requestBody,
                    adminToken,
                    startBarrier
            ));
            Future<Integer> second = executor.submit(() -> createCategoryAfterBarrier(
                    requestBody,
                    adminToken,
                    startBarrier
            ));

            assertThat(List.of(
                    first.get(30, TimeUnit.SECONDS),
                    second.get(30, TimeUnit.SECONDS)
            )).containsExactlyInAnyOrder(201, 409);
        } finally {
            executor.shutdownNow();
        }

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM categories WHERE slug = ?",
                Integer.class,
                slug
        )).isOne();
    }

    @Test
    void enforcesProductAdminAuthorizationAndCreatesOnlyAnAllowlistedProduct() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Admin Product Category " + suffix,
                "admin-product-category-" + suffix,
                null,
                null
        ));
        String requestBody = productJson(
                "Admin Product " + suffix,
                "admin-product-" + suffix,
                "Created product",
                "199.90",
                category.getId()
        );

        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        mockMvc.perform(post("/api/v1/admin/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenForRole(UserRole.CUSTOMER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        var created = mockMvc.perform(post("/api/v1/admin/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenForRole(UserRole.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Admin Product " + suffix))
                .andExpect(jsonPath("$.slug").value("admin-product-" + suffix))
                .andExpect(jsonPath("$.basePrice").value(199.90))
                .andExpect(jsonPath("$.active").doesNotExist())
                .andExpect(jsonPath("$.version").doesNotExist())
                .andReturn();

        UUID productId = UUID.fromString(objectMapper.readTree(
                created.getResponse().getContentAsByteArray()
        ).path("id").asText());
        assertThat(productRepository.findById(productId)).isPresent();
    }

    @Test
    void validatesProductCategoryAndSlugConflictsWithoutLeakingDetails() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String adminToken = accessTokenForRole(UserRole.ADMIN);
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Product Validation Category " + suffix,
                "product-validation-category-" + suffix,
                null,
                null
        ));
        String slug = "validated-product-" + suffix;

        mockMvc.perform(post("/api/v1/admin/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("First Product", slug, null, "10.00", category.getId())))
                .andExpect(status().isCreated());

        var duplicate = mockMvc.perform(post("/api/v1/admin/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Duplicate Product", slug.toUpperCase(Locale.ROOT), null, "10.00", category.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCT_SLUG_ALREADY_EXISTS"))
                .andReturn();
        assertThat(duplicate.getResponse().getContentAsString())
                .doesNotContain("uk_products_slug", "Duplicate entry");

        mockMvc.perform(post("/api/v1/admin/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Missing Category", "missing-category-" + suffix, null, "10.00", Long.MAX_VALUE)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_CATEGORY_NOT_FOUND"));

        category.deactivate();
        categoryRepository.saveAndFlush(category);
        mockMvc.perform(post("/api/v1/admin/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Invalid Price", "invalid-price-" + suffix, null, "10.123", category.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.basePrice").exists());
    }

    @Test
    @Transactional
    void persistsProductsWithBinaryUuidsAndCategoryReferences() {
        String suffix = UUID.randomUUID().toString();
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Persistence Category " + suffix,
                "persistence-category-" + suffix,
                null,
                null
        ));
        Product product = productRepository.saveAndFlush(Product.create(
                "  Persistence Product " + suffix + "  ",
                "PERSISTENCE-PRODUCT-" + suffix.toUpperCase(Locale.ROOT),
                "  Stored product description  ",
                new BigDecimal("199.90"),
                category.getId()
        ));
        UUID productId = product.getId();
        entityManager.clear();

        Product stored = productRepository.findById(productId).orElseThrow();
        assertThat(stored.getId()).isEqualTo(productId);
        assertThat(stored.getName()).isEqualTo("Persistence Product " + suffix);
        assertThat(stored.getSlug()).isEqualTo("persistence-product-" + suffix);
        assertThat(stored.getDescription()).isEqualTo("Stored product description");
        assertThat(stored.getBasePrice()).isEqualByComparingTo("199.90");
        assertThat(stored.getCategoryId()).isEqualTo(category.getId());
        assertThat(stored.isActive()).isTrue();
        assertThat(stored.getVersion()).isZero();
        assertThat(stored.getCreatedAt()).isNotNull();
        assertThat(stored.getUpdatedAt()).isNotNull();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT OCTET_LENGTH(id) FROM products WHERE slug = ?",
                Integer.class,
                stored.getSlug()
        )).isEqualTo(16);
    }

    @Test
    void createsProductVariantsForAdminsAndRejectsDuplicateSkus() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Variant API Category " + suffix, "variant-api-category-" + suffix, null, null
        ));
        Product product = productRepository.saveAndFlush(Product.create(
                "Variant API Product " + suffix, "variant-api-product-" + suffix, null,
                new BigDecimal("50.00"), category.getId()
        ));
        String path = "/api/v1/admin/products/" + product.getId() + "/variants";
        String body = objectMapper.writeValueAsString(Map.of(
                "sku", "api-" + suffix, "size", " M ", "color", " Black ",
                "price", new BigDecimal("55.00"), "stock", 8
        ));

        mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenForRole(UserRole.CUSTOMER))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        String adminToken = accessTokenForRole(UserRole.ADMIN);
        mockMvc.perform(post(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("API-" + suffix.toUpperCase(Locale.ROOT)))
                .andExpect(jsonPath("$.size").value("M"))
                .andExpect(jsonPath("$.active").doesNotExist())
                .andExpect(jsonPath("$.version").value(0));
        mockMvc.perform(post(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("VARIANT_SKU_ALREADY_EXISTS"));
    }

    @Test
    void updatesVariantCatalogWithoutChangingOwnershipStockOrState() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Variant Update Category " + suffix, "variant-update-category-" + suffix, null, null
        ));
        Product product = productRepository.saveAndFlush(Product.create(
                "Variant Update Product " + suffix, "variant-update-product-" + suffix, null,
                new BigDecimal("50.00"), category.getId()
        ));
        ProductVariant variant = productVariantRepository.saveAndFlush(ProductVariant.create(
                product.getId(), "BEFORE-" + suffix, "S", "White", new BigDecimal("51.00"), 7
        ));
        String path = "/api/v1/admin/products/" + product.getId() + "/variants/" + variant.getId();
        String body = objectMapper.writeValueAsString(Map.of(
                "sku", "after-" + suffix, "size", " M ", "color", " Black ",
                "price", new BigDecimal("60.00"), "version", variant.getVersion()
        ));
        String adminToken = accessTokenForRole(UserRole.ADMIN);

        mockMvc.perform(put(path).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("AFTER-" + suffix.toUpperCase(Locale.ROOT)))
                .andExpect(jsonPath("$.stock").value(7))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.active").doesNotExist());

        entityManager.clear();
        ProductVariant updated = productVariantRepository.findById(variant.getId()).orElseThrow();
        assertThat(updated.getProductId()).isEqualTo(product.getId());
        assertThat(updated.getStock()).isEqualTo(7);
        assertThat(updated.isActive()).isTrue();
        mockMvc.perform(put(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("VARIANT_VERSION_CONFLICT"));
    }

    @Test
    void adjustsVariantInventoryForAdminsWithoutChangingCatalogFields() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Inventory Category " + suffix, "inventory-category-" + suffix, null, null
        ));
        Product product = productRepository.saveAndFlush(Product.create(
                "Inventory Product " + suffix, "inventory-product-" + suffix, null,
                new BigDecimal("50.00"), category.getId()
        ));
        ProductVariant variant = productVariantRepository.saveAndFlush(ProductVariant.create(
                product.getId(), "INVENTORY-" + suffix, "M", "Black", new BigDecimal("55.00"), 4
        ));
        String path = "/api/v1/admin/products/" + product.getId()
                + "/variants/" + variant.getId() + "/inventory";
        String body = objectMapper.writeValueAsString(Map.of("stock", 17, "version", variant.getVersion()));
        String adminToken = accessTokenForRole(UserRole.ADMIN);

        mockMvc.perform(patch(path).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenForRole(UserRole.CUSTOMER))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.variantId").value(variant.getId().toString()))
                .andExpect(jsonPath("$.stock").value(17))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.updatedAt").exists())
                .andExpect(jsonPath("$.productId").doesNotExist())
                .andExpect(jsonPath("$.sku").doesNotExist())
                .andExpect(jsonPath("$.active").doesNotExist());

        entityManager.clear();
        ProductVariant updated = productVariantRepository.findById(variant.getId()).orElseThrow();
        assertThat(updated.getStock()).isEqualTo(17);
        assertThat(updated.getSku()).isEqualTo(variant.getSku());
        assertThat(updated.getPrice()).isEqualByComparingTo(variant.getPrice());
        assertThat(updated.isActive()).isTrue();
        mockMvc.perform(patch(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("VARIANT_VERSION_CONFLICT"));
    }

    @Test
    void validatesInventoryOwnershipAndAllowsInactiveCatalogRecords() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Inventory Mapping Category " + suffix, "inventory-mapping-category-" + suffix, null, null
        ));
        Product product = productRepository.saveAndFlush(Product.create(
                "Inventory Mapping Product " + suffix, "inventory-mapping-product-" + suffix, null,
                new BigDecimal("50.00"), category.getId()
        ));
        Product otherProduct = productRepository.saveAndFlush(Product.create(
                "Other Inventory Product " + suffix, "other-inventory-product-" + suffix, null,
                new BigDecimal("45.00"), category.getId()
        ));
        ProductVariant variant = productVariantRepository.saveAndFlush(ProductVariant.create(
                product.getId(), "INVENTORY-MAPPING-" + suffix, "M", "Black", new BigDecimal("55.00"), 4
        ));
        String adminToken = accessTokenForRole(UserRole.ADMIN);
        String path = "/api/v1/admin/products/" + product.getId()
                + "/variants/" + variant.getId() + "/inventory";

        mockMvc.perform(patch("/api/v1/admin/products/{productId}/variants/{variantId}/inventory",
                        otherProduct.getId(), variant.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\":3,\"version\":0}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("VARIANT_NOT_FOUND"));
        mockMvc.perform(patch("/api/v1/admin/products/not-a-uuid/variants/{variantId}/inventory", variant.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\":3,\"version\":0}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("VARIANT_PRODUCT_NOT_FOUND"));
        mockMvc.perform(patch("/api/v1/admin/products/{productId}/variants/not-a-uuid/inventory", product.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\":3,\"version\":0}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("VARIANT_NOT_FOUND"));
        mockMvc.perform(patch("/api/v1/admin/products/00000000-0000-0000-0000-000000000000/variants/{variantId}/inventory", variant.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\":3,\"version\":0}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("VARIANT_PRODUCT_NOT_FOUND"));
        mockMvc.perform(patch(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\":-1,\"version\":0}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\":3,\"version\":0,\"sku\":\"MASS-ASSIGNMENT\"}"))
                .andExpect(status().isBadRequest());

        variant.deactivate();
        product.deactivate();
        productVariantRepository.saveAndFlush(variant);
        productRepository.saveAndFlush(product);
        mockMvc.perform(patch(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\":9,\"version\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(9))
                .andExpect(jsonPath("$.version").value(2));
    }

    @Test
    void serializesConcurrentInventoryAdjustmentsWithExpectedVersion() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Concurrent Inventory Category " + suffix, "concurrent-inventory-category-" + suffix, null, null
        ));
        Product product = productRepository.saveAndFlush(Product.create(
                "Concurrent Inventory Product " + suffix, "concurrent-inventory-product-" + suffix, null,
                new BigDecimal("50.00"), category.getId()
        ));
        ProductVariant variant = productVariantRepository.saveAndFlush(ProductVariant.create(
                product.getId(), "CONCURRENT-INVENTORY-" + suffix, "M", "Black", new BigDecimal("55.00"), 4
        ));
        String path = "/api/v1/admin/products/" + product.getId()
                + "/variants/" + variant.getId() + "/inventory";
        String adminToken = accessTokenForRole(UserRole.ADMIN);
        CyclicBarrier barrier = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Integer> first = executor.submit(() -> patchInventoryAfterBarrier(path, adminToken, 0, 10, barrier));
            Future<Integer> second = executor.submit(() -> patchInventoryAfterBarrier(path, adminToken, 0, 20, barrier));
            assertThat(List.of(
                    first.get(30, TimeUnit.SECONDS),
                    second.get(30, TimeUnit.SECONDS)
            )).containsExactlyInAnyOrder(200, 409);
        } finally {
            executor.shutdownNow();
        }

        entityManager.clear();
        ProductVariant updated = productVariantRepository.findById(variant.getId()).orElseThrow();
        assertThat(updated.getStock()).isIn(10, 20);
        assertThat(updated.getVersion()).isEqualTo(1L);
    }

    private int patchInventoryAfterBarrier(
            String path,
            String adminToken,
            long version,
            int stock,
            CyclicBarrier barrier
    ) throws Exception {
        barrier.await(30, TimeUnit.SECONDS);
        return mockMvc.perform(patch(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "stock", stock,
                                "version", version
                        ))))
                .andReturn()
                .getResponse()
                .getStatus();
    }

    @Test
    void deactivatesVariantsForAdminsAndKeepsTheOperationIdempotent() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Variant Deactivate Category " + suffix,
                "variant-deactivate-category-" + suffix,
                null,
                null
        ));
        Product product = productRepository.saveAndFlush(Product.create(
                "Variant Deactivate Product " + suffix,
                "variant-deactivate-product-" + suffix,
                null,
                new BigDecimal("50.00"),
                category.getId()
        ));
        ProductVariant variant = productVariantRepository.saveAndFlush(ProductVariant.create(
                product.getId(), "DEACTIVATE-" + suffix, "M", "Black", new BigDecimal("55.00"), 4
        ));
        String path = "/api/v1/admin/products/" + product.getId()
                + "/variants/" + variant.getId() + "/deactivate";

        mockMvc.perform(patch(path))
                .andExpect(status().isUnauthorized());
        String adminToken = accessTokenForRole(UserRole.ADMIN);
        mockMvc.perform(patch(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenForRole(UserRole.CUSTOMER)))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

        entityManager.clear();
        ProductVariant deactivated = productVariantRepository.findById(variant.getId()).orElseThrow();
        Instant deactivatedAt = deactivated.getUpdatedAt();
        assertThat(deactivated.isActive()).isFalse();
        assertThat(deactivated.getVersion()).isEqualTo(1L);
        mockMvc.perform(get("/api/v1/products/{id}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.variants").isEmpty());

        mockMvc.perform(patch(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent());
        entityManager.clear();
        ProductVariant stillDeactivated = productVariantRepository.findById(variant.getId()).orElseThrow();
        assertThat(stillDeactivated.isActive()).isFalse();
        assertThat(stillDeactivated.getVersion()).isEqualTo(1L);
        assertThat(stillDeactivated.getUpdatedAt()).isEqualTo(deactivatedAt);
    }

    @Test
    void protectsVariantDeactivationOwnershipAndIdMapping() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Variant Deactivate Mapping Category " + suffix,
                "variant-deactivate-mapping-category-" + suffix,
                null,
                null
        ));
        Product product = productRepository.saveAndFlush(Product.create(
                "Variant Deactivate Mapping Product " + suffix,
                "variant-deactivate-mapping-product-" + suffix,
                null,
                new BigDecimal("50.00"),
                category.getId()
        ));
        Product otherProduct = productRepository.saveAndFlush(Product.create(
                "Other Variant Deactivate Product " + suffix,
                "other-variant-deactivate-product-" + suffix,
                null,
                new BigDecimal("45.00"),
                category.getId()
        ));
        ProductVariant variant = productVariantRepository.saveAndFlush(ProductVariant.create(
                product.getId(), "MAPPING-" + suffix, "M", "Black", new BigDecimal("55.00"), 4
        ));
        String adminToken = accessTokenForRole(UserRole.ADMIN);
        String basePath = "/api/v1/admin/products/" + product.getId()
                + "/variants/" + variant.getId() + "/deactivate";

        mockMvc.perform(patch("/api/v1/admin/products/{productId}/variants/{variantId}/deactivate",
                        otherProduct.getId(), variant.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("VARIANT_NOT_FOUND"));
        mockMvc.perform(patch("/api/v1/admin/products/not-a-uuid/variants/{variantId}/deactivate", variant.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("VARIANT_PRODUCT_NOT_FOUND"));
        mockMvc.perform(patch("/api/v1/admin/products/{productId}/variants/not-a-uuid/deactivate", product.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("VARIANT_NOT_FOUND"));
        mockMvc.perform(patch("/api/v1/admin/products/00000000-0000-0000-0000-000000000000/variants/{variantId}/deactivate", variant.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("VARIANT_PRODUCT_NOT_FOUND"));
        product.deactivate();
        productRepository.saveAndFlush(product);
        mockMvc.perform(patch(basePath)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void concurrentlyDeactivatesVariantOnlyOnce() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Concurrent Variant Deactivate Category " + suffix,
                "concurrent-variant-deactivate-category-" + suffix,
                null,
                null
        ));
        Product product = productRepository.saveAndFlush(Product.create(
                "Concurrent Variant Deactivate Product " + suffix,
                "concurrent-variant-deactivate-product-" + suffix,
                null,
                new BigDecimal("50.00"),
                category.getId()
        ));
        ProductVariant variant = productVariantRepository.saveAndFlush(ProductVariant.create(
                product.getId(), "CONCURRENT-DEACTIVATE-" + suffix, "M", "Black", new BigDecimal("55.00"), 4
        ));
        String path = "/api/v1/admin/products/" + product.getId()
                + "/variants/" + variant.getId() + "/deactivate";
        String adminToken = accessTokenForRole(UserRole.ADMIN);
        CyclicBarrier barrier = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Integer> first = executor.submit(() -> patchAfterBarrier(path, adminToken, barrier));
            Future<Integer> second = executor.submit(() -> patchAfterBarrier(path, adminToken, barrier));
            assertThat(List.of(
                    first.get(30, TimeUnit.SECONDS),
                    second.get(30, TimeUnit.SECONDS)
            )).containsExactly(204, 204);
        } finally {
            executor.shutdownNow();
        }

        entityManager.clear();
        ProductVariant deactivated = productVariantRepository.findById(variant.getId()).orElseThrow();
        assertThat(deactivated.isActive()).isFalse();
        assertThat(deactivated.getVersion()).isEqualTo(1L);
    }

    private int patchAfterBarrier(String path, String adminToken, CyclicBarrier barrier) throws Exception {
        barrier.await(30, TimeUnit.SECONDS);
        return mockMvc.perform(patch(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andReturn()
                .getResponse()
                .getStatus();
    }

    @Test
    @Transactional
    void persistsProductVariantsWithBinaryUuidsAndProductReferences() {
        String suffix = UUID.randomUUID().toString();
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Variant Category " + suffix, "variant-category-" + suffix, null, null
        ));
        Product product = productRepository.saveAndFlush(Product.create(
                "Variant Product " + suffix, "variant-product-" + suffix, null,
                new BigDecimal("99.90"), category.getId()
        ));
        ProductVariant variant = productVariantRepository.saveAndFlush(ProductVariant.create(
                product.getId(), " sku-" + suffix + " ", " M ", " Black ",
                new BigDecimal("109.90"), 12
        ));
        UUID variantId = variant.getId();
        entityManager.clear();

        ProductVariant stored = productVariantRepository.findById(variantId).orElseThrow();
        assertThat(stored.getProductId()).isEqualTo(product.getId());
        assertThat(stored.getSku()).isEqualTo("SKU-" + suffix.toUpperCase(Locale.ROOT));
        assertThat(stored.getSize()).isEqualTo("M");
        assertThat(stored.getColor()).isEqualTo("Black");
        assertThat(stored.getPrice()).isEqualByComparingTo("109.90");
        assertThat(stored.getStock()).isEqualTo(12);
        assertThat(stored.isActive()).isTrue();
        assertThat(stored.getVersion()).isZero();
        assertThat(stored.getCreatedAt()).isNotNull();
        assertThat(stored.getUpdatedAt()).isNotNull();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT OCTET_LENGTH(id) FROM product_variants WHERE sku = ?",
                Integer.class, stored.getSku()
        )).isEqualTo(16);
        assertThatThrownBy(() -> ProductVariant.create(
                product.getId(), "ß".repeat(51), "M", "Black", new BigDecimal("10.00"), 0
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100 characters");
    }

    @Test
    void updatesProductsForAdminsWithOptimisticVersioning() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Update Category " + suffix, "update-category-" + suffix, null, null
        ));
        Product product = productRepository.saveAndFlush(Product.create(
                "Before " + suffix, "before-" + suffix, null,
                new BigDecimal("10.00"), category.getId()
        ));
        String path = "/api/v1/admin/products/" + product.getId();
        String body = updateProductJson(
                "After " + suffix, "after-" + suffix, "Updated",
                "20.00", category.getId(), product.getVersion()
        );

        mockMvc.perform(put(path).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenForRole(UserRole.CUSTOMER))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        String adminToken = accessTokenForRole(UserRole.ADMIN);
        mockMvc.perform(put(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("After " + suffix))
                .andExpect(jsonPath("$.version").doesNotExist());

        entityManager.clear();
        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updated.getVersion()).isEqualTo(1L);
        assertThat(updated.getBasePrice()).isEqualByComparingTo("20.00");
        mockMvc.perform(put(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCT_VERSION_CONFLICT"));
    }

    @Test
    void deactivatesProductsForAdminsAndHidesThemIdempotently() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Deactivate Category " + suffix,
                "deactivate-category-" + suffix,
                null,
                null
        ));
        Product product = productRepository.saveAndFlush(Product.create(
                "Deactivate Product " + suffix,
                "deactivate-product-" + suffix,
                "To be hidden",
                new BigDecimal("20.00"),
                category.getId()
        ));
        String path = "/api/v1/admin/products/" + product.getId() + "/deactivate";

        mockMvc.perform(patch(path))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mockMvc.perform(patch(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenForRole(UserRole.CUSTOMER)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        String adminToken = accessTokenForRole(UserRole.ADMIN);
        mockMvc.perform(patch(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

        entityManager.clear();
        Product deactivated = productRepository.findById(product.getId()).orElseThrow();
        assertThat(deactivated.isActive()).isFalse();
        assertThat(deactivated.getVersion()).isEqualTo(1L);
        mockMvc.perform(get("/api/v1/products/" + product.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        mockMvc.perform(patch(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent());
        assertThat(productRepository.findById(product.getId()).orElseThrow().isActive()).isFalse();
    }

    @Test
    void returnsProductNotFoundForMalformedOrMissingDeactivateIds() throws Exception {
        String adminToken = accessTokenForRole(UserRole.ADMIN);
        mockMvc.perform(patch("/api/v1/admin/products/not-a-uuid/deactivate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        mockMvc.perform(patch("/api/v1/admin/products/00000000-0000-0000-0000-000000000000/deactivate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void exposesOnlyActiveProductsAndActiveDetailsToAnonymousClients() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Public Product Category " + suffix,
                "public-product-category-" + suffix,
                null,
                null
        ));

        Product visible = productRepository.saveAndFlush(Product.create(
                "Visible Product " + suffix,
                "visible-product-" + suffix,
                "Public catalog item",
                new BigDecimal("49.99"),
                category.getId()
        ));

        Product hidden = Product.create(
                "Hidden Product " + suffix,
                "hidden-product-" + suffix,
                "Inactive catalog item",
                new BigDecimal("39.99"),
                category.getId()
        );
        hidden.deactivate();
        hidden = productRepository.saveAndFlush(hidden);

        mockMvc.perform(get("/api/v1/products")
                        .param("keyword", suffix)
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(visible.getId().toString()))
                .andExpect(jsonPath("$.content[0].slug").value(visible.getSlug()))
                .andExpect(jsonPath("$.content[0].basePrice").value(49.99))
                .andExpect(jsonPath("$.content[0].categoryId").value(category.getId()))
                .andExpect(jsonPath("$.content[0].active").doesNotExist())
                .andExpect(jsonPath("$.content[0].variants").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/v1/products/{id}", visible.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(visible.getId().toString()))
                .andExpect(jsonPath("$.name").value(visible.getName()))
                .andExpect(jsonPath("$.description").value("Public catalog item"))
                .andExpect(jsonPath("$.active").doesNotExist())
                .andExpect(jsonPath("$.variants").isEmpty());

        mockMvc.perform(get("/api/v1/products/{id}", hidden.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));


        UUID missingId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/products/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/v1/products/" + missingId));

        mockMvc.perform(get("/api/v1/products/not-a-uuid"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void exposesOnlyActiveVariantsForTheRequestedPublicProduct() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Public Variant Category " + suffix,
                "public-variant-category-" + suffix,
                null,
                null
        ));
        Product product = productRepository.saveAndFlush(Product.create(
                "Public Variant Product " + suffix,
                "public-variant-product-" + suffix,
                null,
                new BigDecimal("50.00"),
                category.getId()
        ));
        Product otherProduct = productRepository.saveAndFlush(Product.create(
                "Other Variant Product " + suffix,
                "other-variant-product-" + suffix,
                null,
                new BigDecimal("40.00"),
                category.getId()
        ));
        ProductVariant medium = productVariantRepository.saveAndFlush(ProductVariant.create(
                product.getId(), "PUBLIC-M-" + suffix, "M", "White", new BigDecimal("55.00"), 8
        ));
        ProductVariant large = productVariantRepository.saveAndFlush(ProductVariant.create(
                product.getId(), "PUBLIC-L-" + suffix, "L", "Black", new BigDecimal("56.00"), 3
        ));
        ProductVariant inactive = productVariantRepository.saveAndFlush(ProductVariant.create(
                product.getId(), "PUBLIC-S-" + suffix, "S", "Blue", new BigDecimal("54.00"), 5
        ));
        productVariantRepository.saveAndFlush(ProductVariant.create(
                otherProduct.getId(), "OTHER-" + suffix, "XS", "Green", new BigDecimal("45.00"), 4
        ));
        inactive.deactivate();
        productVariantRepository.saveAndFlush(inactive);
        entityManager.clear();

        mockMvc.perform(get("/api/v1/products/{id}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId().toString()))
                .andExpect(jsonPath("$.variants.length()").value(2))
                .andExpect(jsonPath("$.variants[0].id").value(large.getId().toString()))
                .andExpect(jsonPath("$.variants[0].sku").value(large.getSku()))
                .andExpect(jsonPath("$.variants[0].size").value(large.getSize()))
                .andExpect(jsonPath("$.variants[0].color").value(large.getColor()))
                .andExpect(jsonPath("$.variants[0].price").value(large.getPrice().doubleValue()))
                .andExpect(jsonPath("$.variants[0].stock").value(large.getStock()))
                .andExpect(jsonPath("$.variants[1].id").value(medium.getId().toString()))
                .andExpect(jsonPath("$.variants[0].productId").doesNotExist())
                .andExpect(jsonPath("$.variants[0].active").doesNotExist())
                .andExpect(jsonPath("$.variants[0].version").doesNotExist())
                .andExpect(jsonPath("$.variants[0].createdAt").doesNotExist())
                .andExpect(jsonPath("$.variants[0].updatedAt").doesNotExist());
    }

    @Test
    void attachesHttpsProductImagesForAdminsAndExposesThemOnPublicDetail() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Image Category " + suffix, "image-category-" + suffix, null, null
        ));
        Product product = productRepository.saveAndFlush(Product.create(
                "Image Product " + suffix, "image-product-" + suffix, null,
                new BigDecimal("50.00"), category.getId()
        ));
        ProductVariant variant = productVariantRepository.saveAndFlush(ProductVariant.create(
                product.getId(), "IMAGE-" + suffix, "M", "Black", new BigDecimal("55.00"), 4
        ));
        String path = "/api/v1/admin/products/" + product.getId() + "/images";
        String adminToken = accessTokenForRole(UserRole.ADMIN);
        String body = objectMapper.writeValueAsString(Map.of(
                "url", "https://cdn.example.test/" + suffix + ".jpg",
                "variantId", variant.getId(),
                "primary", true,
                "sortOrder", 1
        ));

        mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenForRole(UserRole.CUSTOMER))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(post(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"http://cdn.example.test/x.jpg\",\"primary\":false,\"sortOrder\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PRODUCT_IMAGE_URL"));
        mockMvc.perform(post(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value("https://cdn.example.test/" + suffix + ".jpg"))
                .andExpect(jsonPath("$.variantId").value(variant.getId().toString()))
                .andExpect(jsonPath("$.primary").value(true))
                .andExpect(jsonPath("$.productId").doesNotExist());

        mockMvc.perform(get("/api/v1/products/{id}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.images.length()").value(1))
                .andExpect(jsonPath("$.images[0].url").value("https://cdn.example.test/" + suffix + ".jpg"))
                .andExpect(jsonPath("$.images[0].primary").value(true));
        mockMvc.perform(get("/api/v1/products").param("keyword", suffix))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].images").doesNotExist());
    }

    @Test
    void rejectsReviewUntilTheCustomerHasADeliveredOrder() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Review Category " + suffix, "review-category-" + suffix, null, null
        ));
        Product product = productRepository.saveAndFlush(Product.create(
                "Review Product " + suffix, "review-product-" + suffix, null,
                new BigDecimal("50.00"), category.getId()
        ));
        String customerToken = accessTokenForRole(UserRole.CUSTOMER);
        mockMvc.perform(post("/api/v1/products/{id}/reviews", product.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5,\"comment\":\"Nice\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("REVIEW_NOT_ALLOWED"));
        mockMvc.perform(get("/api/v1/products/{id}/reviews", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/v1/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenForRole(UserRole.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
    }

    @Test
    void createsCodOrderFromCartDecrementsStockAndRestoresItOnCancel() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Order Category " + suffix, "order-category-" + suffix, null, null
        ));
        Product product = productRepository.saveAndFlush(Product.create(
                "Order Product " + suffix, "order-product-" + suffix, null,
                new BigDecimal("50.00"), category.getId()
        ));
        ProductVariant variant = productVariantRepository.saveAndFlush(ProductVariant.create(
                product.getId(), "ORDER-" + suffix, "M", "Black", new BigDecimal("55.00"), 4
        ));
        String customerToken = accessTokenForRole(UserRole.CUSTOMER);
        mockMvc.perform(post("/api/v1/cart/items")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "variantId", variant.getId(),
                                "quantity", 2
                        ))))
                .andExpect(status().isCreated());
        var created = mockMvc.perform(post("/api/v1/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "shippingAddress", "12 Test Street",
                                "shippingPhone", "0900000000",
                                "paymentMethod", "COD"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.paymentMethod").value("COD"))
                .andExpect(jsonPath("$.paymentStatus").value("UNPAID"))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andReturn();
        String orderId = objectMapper.readTree(created.getResponse().getContentAsByteArray()).path("id").asText();
        entityManager.clear();
        assertThat(productVariantRepository.findById(variant.getId()).orElseThrow().getStock()).isEqualTo(2);
        mockMvc.perform(get("/api/v1/cart")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
                .andExpect(jsonPath("$.items").isEmpty());
        mockMvc.perform(put("/api/v1/orders/{id}/cancel", orderId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        entityManager.clear();
        assertThat(productVariantRepository.findById(variant.getId()).orElseThrow().getStock()).isEqualTo(4);
    }

    @Test
    void managesCustomerCartQuantityAgainstVariantStock() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Cart Category " + suffix, "cart-category-" + suffix, null, null
        ));
        Product product = productRepository.saveAndFlush(Product.create(
                "Cart Product " + suffix, "cart-product-" + suffix, null,
                new BigDecimal("50.00"), category.getId()
        ));
        ProductVariant variant = productVariantRepository.saveAndFlush(ProductVariant.create(
                product.getId(), "CART-" + suffix, "M", "Black", new BigDecimal("55.00"), 4
        ));
        String customerToken = accessTokenForRole(UserRole.CUSTOMER);
        String addBody = objectMapper.writeValueAsString(Map.of(
                "variantId", variant.getId(),
                "quantity", 2
        ));

        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/cart/items")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].sku").value(variant.getSku()))
                .andExpect(jsonPath("$.totalAmount").value(110.00));
        mockMvc.perform(post("/api/v1/cart/items")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "variantId", variant.getId(),
                                "quantity", 3
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));
        mockMvc.perform(get("/api/v1/cart")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(2));
        mockMvc.perform(delete("/api/v1/cart")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/cart")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalAmount").value(0));
    }

    @Test
    void hidesProductsWhenTheirCategoryIsInactive() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Inactive Parent Category " + suffix,
                "inactive-parent-category-" + suffix,
                null,
                null
        ));
        Product product = productRepository.saveAndFlush(Product.create(
                "Orphaned Public Product " + suffix,
                "orphaned-public-product-" + suffix,
                null,
                new BigDecimal("15.00"),
                category.getId()
        ));
        category.deactivate();
        categoryRepository.saveAndFlush(category);

        mockMvc.perform(get("/api/v1/products").param("keyword", suffix))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(get("/api/v1/products/{id}", product.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void filtersSortsAndPaginatesThePublicProductCatalog() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category selectedCategory = categoryRepository.saveAndFlush(Category.create(
                "Selected Product Category " + suffix,
                "selected-product-category-" + suffix,
                null,
                null
        ));
        Category otherCategory = categoryRepository.saveAndFlush(Category.create(
                "Other Product Category " + suffix,
                "other-product-category-" + suffix,
                null,
                null
        ));
        Product lowerPrice = productRepository.saveAndFlush(Product.create(
                "Catalog Match Budget " + suffix,
                "catalog-match-budget-" + suffix,
                null,
                new BigDecimal("20.00"),
                selectedCategory.getId()
        ));
        Product higherPrice = productRepository.saveAndFlush(Product.create(
                "Catalog Match Premium " + suffix,
                "catalog-match-premium-" + suffix,
                null,
                new BigDecimal("30.00"),
                selectedCategory.getId()
        ));
        productRepository.saveAndFlush(Product.create(
                "Catalog Match Too Cheap " + suffix,
                "catalog-match-too-cheap-" + suffix,
                null,
                new BigDecimal("10.00"),
                selectedCategory.getId()
        ));
        productRepository.saveAndFlush(Product.create(
                "Catalog Match Other Category " + suffix,
                "catalog-match-other-category-" + suffix,
                null,
                new BigDecimal("25.00"),
                otherCategory.getId()
        ));

        mockMvc.perform(get("/api/v1/products")
                        .param("keyword", "CATALOG MATCH")
                        .param("category", selectedCategory.getSlug().toUpperCase(Locale.ROOT))
                        .param("minPrice", "15.00")
                        .param("maxPrice", "30.00")
                        .param("sort", "price,desc")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(higherPrice.getId().toString()))
                .andExpect(jsonPath("$.content[0].basePrice").value(30.0))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));

        mockMvc.perform(get("/api/v1/products")
                        .param("keyword", "catalog match")
                        .param("category", selectedCategory.getSlug())
                        .param("minPrice", "15")
                        .param("maxPrice", "30")
                        .param("sort", "price,desc")
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(lowerPrice.getId().toString()))
                .andExpect(jsonPath("$.page").value(1));
    }

    @Test
    void rejectsInvalidPublicProductQueriesWithoutLeakingDetails() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_PRODUCT_QUERY"))
                .andExpect(jsonPath("$.path").value("/api/v1/products"));

        mockMvc.perform(get("/api/v1/products").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PRODUCT_QUERY"));

        mockMvc.perform(get("/api/v1/products").param("sort", "id,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PRODUCT_QUERY"));

        mockMvc.perform(get("/api/v1/products").param("minPrice", "1e2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PRODUCT_QUERY"));

        mockMvc.perform(get("/api/v1/products").param("maxPrice", "10000000000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PRODUCT_QUERY"));

        mockMvc.perform(get("/api/v1/products")
                        .param("minPrice", "20.00")
                        .param("maxPrice", "10.00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PRODUCT_QUERY"));

        mockMvc.perform(get("/api/v1/products").param("variantSize", "x".repeat(21)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PRODUCT_QUERY"));

        mockMvc.perform(get("/api/v1/products").param("color", "c".repeat(51)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PRODUCT_QUERY"));
    }

    @Test
    void filtersPublicProductsByActiveVariantSizeAndColor() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Category category = categoryRepository.saveAndFlush(Category.create(
                "Variant Filter Category " + suffix,
                "variant-filter-category-" + suffix,
                null,
                null
        ));
        Product matching = productRepository.saveAndFlush(Product.create(
                "Matching Variant Filter " + suffix,
                "matching-variant-filter-" + suffix,
                null,
                new BigDecimal("40.00"),
                category.getId()
        ));
        Product splitAttributes = productRepository.saveAndFlush(Product.create(
                "Split Variant Filter " + suffix,
                "split-variant-filter-" + suffix,
                null,
                new BigDecimal("41.00"),
                category.getId()
        ));
        Product inactiveVariantOnly = productRepository.saveAndFlush(Product.create(
                "Inactive Variant Filter " + suffix,
                "inactive-variant-filter-" + suffix,
                null,
                new BigDecimal("42.00"),
                category.getId()
        ));
        productVariantRepository.saveAndFlush(ProductVariant.create(
                matching.getId(), "FILTER-M-BLACK-" + suffix, "M", "Black", new BigDecimal("40.00"), 4
        ));
        productVariantRepository.saveAndFlush(ProductVariant.create(
                matching.getId(), "FILTER-L-WHITE-" + suffix, "L", "White", new BigDecimal("41.00"), 2
        ));
        productVariantRepository.saveAndFlush(ProductVariant.create(
                splitAttributes.getId(), "FILTER-M-WHITE-" + suffix, "M", "White", new BigDecimal("41.00"), 3
        ));
        productVariantRepository.saveAndFlush(ProductVariant.create(
                splitAttributes.getId(), "FILTER-L-BLACK-" + suffix, "L", "Black", new BigDecimal("42.00"), 3
        ));
        ProductVariant inactive = productVariantRepository.saveAndFlush(ProductVariant.create(
                inactiveVariantOnly.getId(), "FILTER-M-BLACK-INACTIVE-" + suffix, "M", "Black",
                new BigDecimal("42.00"), 5
        ));
        inactive.deactivate();
        productVariantRepository.saveAndFlush(inactive);

        mockMvc.perform(get("/api/v1/products")
                        .param("keyword", suffix)
                        .param("variantSize", "m")
                        .param("color", "BLACK")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(matching.getId().toString()))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/v1/products")
                        .param("keyword", suffix)
                        .param("variantSize", "M"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(get("/api/v1/products")
                        .param("keyword", suffix)
                        .param("color", "black")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void startsWithValidatedIdentityMigration() {
        var currentMigration = flyway.info().current();

        assertThat(MYSQL.isRunning()).isTrue();
        assertThat(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).isEqualTo(1);
        assertThat(currentMigration).isNotNull();
        assertThat(currentMigration.getVersion()).isEqualTo(MigrationVersion.fromVersion("8"));
        assertThat(currentMigration.getDescription()).isEqualTo("create reviews");
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
                  AND table_name IN (
                      'users',
                      'refresh_sessions',
                      'categories',
                      'products',
                      'product_variants',
                      'product_images',
                      'carts',
                      'cart_items',
                      'orders',
                      'order_items',
                      'reviews',
                      'flyway_schema_history'
                  )
                """,
                Integer.class
        );
        assertThat(expectedTables).isEqualTo(12);
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

    private String categoryJson(
            String name,
            String slug,
            String description,
            Long parentId
    ) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", name);
        payload.put("slug", slug);
        payload.put("description", description);
        payload.put("parentId", parentId);
        return objectMapper.writeValueAsString(payload);
    }

    private String updateProductJson(
            String name, String slug, String description, String basePrice,
            Long categoryId, long version
    ) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", name);
        payload.put("slug", slug);
        payload.put("description", description);
        payload.put("basePrice", new BigDecimal(basePrice));
        payload.put("categoryId", categoryId);
        payload.put("version", version);
        return objectMapper.writeValueAsString(payload);
    }

    private String productJson(
            String name, String slug, String description, String basePrice, Long categoryId
    ) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", name);
        payload.put("slug", slug);
        payload.put("description", description);
        payload.put("basePrice", new BigDecimal(basePrice));
        payload.put("categoryId", categoryId);
        return objectMapper.writeValueAsString(payload);
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

    private int createCategoryAfterBarrier(
            String requestBody,
            String accessToken,
            CyclicBarrier startBarrier
    ) {
        await(startBarrier);
        try {
            return mockMvc.perform(post("/api/v1/admin/categories")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andReturn()
                    .getResponse()
                    .getStatus();
        } catch (Exception exception) {
            throw new IllegalStateException("concurrent category request failed", exception);
        }
    }

    private String accessTokenForRole(UserRole role) {
        String email = "role-" + role.name().toLowerCase(Locale.ROOT)
                + "-" + UUID.randomUUID() + "@example.com";
        User user = userRepository.saveAndFlush(User.createCustomer(
                email,
                PASSWORD_HASH,
                "Role Boundary",
                null
        ));
        if (role == UserRole.ADMIN) {
            assertThat(jdbcTemplate.update(
                    "UPDATE users SET role = 'ADMIN' WHERE id = ?",
                    uuidBytes(user.getId())
            )).isOne();
            entityManager.clear();
            user = userRepository.findById(user.getId()).orElseThrow();
        }
        return accessTokenService.issue(user).value();
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
