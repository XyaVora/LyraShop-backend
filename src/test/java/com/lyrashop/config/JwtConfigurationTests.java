package com.lyrashop.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import com.lyrashop.auth.service.AccessTokenService;
import com.lyrashop.security.AccessTokenClaimsValidator;
import com.lyrashop.user.entity.User;
import com.lyrashop.user.entity.UserRole;

import jakarta.validation.Validation;

class JwtConfigurationTests {

    private static final String VALID_SECRET = Base64.getEncoder().encodeToString(
            "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8)
    );
    private static final Instant NOW = Instant.parse("2026-08-11T12:00:00Z");
    private static final JwtProperties PROPERTIES = new JwtProperties(
            "https://api.lyrashop.test",
            "lyrashop-api",
            Duration.ofMinutes(15),
            VALID_SECRET
    );

    private final JwtConfiguration configuration = new JwtConfiguration();

    @Test
    void issuesMinimalHs256AccessTokensWithRoleAuthorities() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        SecretKey secretKey = configuration.jwtSecretKey(PROPERTIES);
        var encoder = configuration.jwtEncoder(secretKey);
        JwtDecoder decoder = configuration.jwtDecoder(secretKey, PROPERTIES, clock);
        AccessTokenService tokenService = new AccessTokenService(encoder, PROPERTIES, clock);
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getRole()).thenReturn(UserRole.CUSTOMER);
        when(user.isActive()).thenReturn(true);

        var first = tokenService.issue(user);
        var second = tokenService.issue(user);
        Jwt token = decoder.decode(first.value());

        assertThat(first.expiresInSeconds()).isEqualTo(900);
        assertThat(first.value()).isNotEqualTo(second.value());
        assertThat(first.toString())
                .contains("[REDACTED]")
                .doesNotContain(first.value());
        assertThat(PROPERTIES.toString())
                .contains("[REDACTED]")
                .doesNotContain(VALID_SECRET);
        assertThat(token.getHeaders())
                .containsEntry("alg", "HS256")
                .containsEntry("typ", "JWT");
        assertThat(token.getIssuer().toString()).isEqualTo(PROPERTIES.issuer());
        assertThat(token.getSubject()).isEqualTo(userId.toString());
        assertThat(token.getAudience()).containsExactly(PROPERTIES.audience());
        assertThat(token.getIssuedAt()).isEqualTo(NOW);
        assertThat(token.getExpiresAt()).isEqualTo(NOW.plus(Duration.ofMinutes(15)));
        assertThat(token.getId()).isNotBlank();
        assertThat(token.getClaimAsString(AccessTokenClaimsValidator.TOKEN_USE_CLAIM))
                .isEqualTo(AccessTokenClaimsValidator.ACCESS_TOKEN_USE);
        assertThat(token.getClaimAsStringList(AccessTokenClaimsValidator.ROLES_CLAIM))
                .containsExactly(UserRole.CUSTOMER.name());
        assertThat(token.getClaims()).doesNotContainKeys(
                "email",
                "fullName",
                "phone",
                "password",
                "passwordHash",
                "active",
                "version"
        );

        var authentication = configuration.jwtAuthenticationConverter().convert(token);
        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_CUSTOMER");
    }

    @Test
    void decoderRejectsWrongSignatureIssuerAudienceAndExpiredTokens() {
        Clock issuingClock = Clock.fixed(NOW, ZoneOffset.UTC);
        SecretKey secretKey = configuration.jwtSecretKey(PROPERTIES);
        var encoder = configuration.jwtEncoder(secretKey);
        var issued = new AccessTokenService(encoder, PROPERTIES, issuingClock)
                .issue(user(UserRole.ADMIN));
        JwtDecoder decoder = configuration.jwtDecoder(secretKey, PROPERTIES, issuingClock);

        JwtProperties otherKeyProperties = new JwtProperties(
                PROPERTIES.issuer(),
                PROPERTIES.audience(),
                PROPERTIES.accessTokenTtl(),
                Base64.getEncoder().encodeToString(
                        "fedcba9876543210fedcba9876543210".getBytes(StandardCharsets.UTF_8)
                )
        );
        String wrongSignatureToken = new AccessTokenService(
                configuration.jwtEncoder(configuration.jwtSecretKey(otherKeyProperties)),
                otherKeyProperties,
                issuingClock
        ).issue(user(UserRole.ADMIN)).value();

        assertThatThrownBy(() -> decoder.decode(wrongSignatureToken))
                .isInstanceOf(JwtException.class);

        JwtProperties wrongIssuer = new JwtProperties(
                "other-issuer",
                PROPERTIES.audience(),
                PROPERTIES.accessTokenTtl(),
                VALID_SECRET
        );
        String wrongIssuerToken = new AccessTokenService(
                encoder,
                wrongIssuer,
                issuingClock
        ).issue(user(UserRole.ADMIN)).value();
        assertThatThrownBy(() -> decoder.decode(wrongIssuerToken))
                .isInstanceOf(JwtValidationException.class);

        JwtProperties wrongAudience = new JwtProperties(
                PROPERTIES.issuer(),
                "other-audience",
                PROPERTIES.accessTokenTtl(),
                VALID_SECRET
        );
        String wrongAudienceToken = new AccessTokenService(
                encoder,
                wrongAudience,
                issuingClock
        ).issue(user(UserRole.ADMIN)).value();
        assertThatThrownBy(() -> decoder.decode(wrongAudienceToken))
                .isInstanceOf(JwtValidationException.class);

        Clock afterExpiry = Clock.fixed(
                NOW.plus(PROPERTIES.accessTokenTtl()).plus(JwtConfiguration.CLOCK_SKEW)
                        .plusSeconds(1),
                ZoneOffset.UTC
        );
        JwtDecoder expiredDecoder =
                configuration.jwtDecoder(secretKey, PROPERTIES, afterExpiry);
        assertThatThrownBy(() -> expiredDecoder.decode(issued.value()))
                .isInstanceOf(JwtValidationException.class);
    }

    @Test
    void validatesSecretStrengthAndJwtPropertyBounds() {
        JwtProperties invalidBase64 = new JwtProperties(
                PROPERTIES.issuer(),
                PROPERTIES.audience(),
                PROPERTIES.accessTokenTtl(),
                "not-base64!"
        );
        assertThatThrownBy(() -> configuration.jwtSecretKey(invalidBase64))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT signing secret must be valid Base64");

        JwtProperties shortSecret = new JwtProperties(
                PROPERTIES.issuer(),
                PROPERTIES.audience(),
                PROPERTIES.accessTokenTtl(),
                Base64.getEncoder().encodeToString(new byte[31])
        );
        assertThatThrownBy(() -> configuration.jwtSecretKey(shortSecret))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");

        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertThat(validator.validate(propertiesWithTtl(Duration.ofMinutes(15)))).isEmpty();
            assertThat(validator.validate(propertiesWithTtl(Duration.ofMinutes(30)))).isEmpty();
            assertThat(validator.validate(propertiesWithTtl(
                    Duration.ofMinutes(15).minusNanos(1)
            ))).extracting("propertyPath").extracting(Object::toString)
                    .contains("accessTokenTtl");
            assertThat(validator.validate(propertiesWithTtl(
                    Duration.ofMinutes(30).plusNanos(1)
            ))).extracting("propertyPath").extracting(Object::toString)
                    .contains("accessTokenTtl");
        }
    }

    @Test
    void refusesToIssueAccessTokensForInactiveUsers() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        SecretKey secretKey = configuration.jwtSecretKey(PROPERTIES);
        AccessTokenService tokenService =
                new AccessTokenService(configuration.jwtEncoder(secretKey), PROPERTIES, clock);
        User user = mock(User.class);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(user.getRole()).thenReturn(UserRole.CUSTOMER);
        when(user.isActive()).thenReturn(false);

        assertThatThrownBy(() -> tokenService.issue(user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("inactive users");
    }

    private static JwtProperties propertiesWithTtl(Duration ttl) {
        return new JwtProperties(
                PROPERTIES.issuer(),
                PROPERTIES.audience(),
                ttl,
                VALID_SECRET
        );
    }

    private static User user(UserRole role) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(user.getRole()).thenReturn(role);
        when(user.isActive()).thenReturn(true);
        return user;
    }
}
