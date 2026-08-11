package com.lyrashop.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import com.lyrashop.user.entity.UserRole;

class AccessTokenClaimsValidatorTests {

    private static final Instant ISSUED_AT = Instant.parse("2026-08-11T12:00:00Z");
    private static final AccessTokenClaimsValidator VALIDATOR =
            new AccessTokenClaimsValidator(Duration.ofMinutes(30));

    @Test
    void acceptsOnlyAccessUseUuidSubjectKnownRoleAndBoundedLifetime() {
        assertThat(VALIDATOR.validate(token(
                UUID.randomUUID().toString(),
                AccessTokenClaimsValidator.ACCESS_TOKEN_USE,
                List.of(UserRole.ADMIN.name()),
                ISSUED_AT.plus(Duration.ofMinutes(30))
        )).hasErrors()).isFalse();

        assertInvalid(token(
                "not-a-uuid",
                AccessTokenClaimsValidator.ACCESS_TOKEN_USE,
                List.of(UserRole.CUSTOMER.name()),
                ISSUED_AT.plus(Duration.ofMinutes(15))
        ));
        assertInvalid(token(
                UUID.randomUUID().toString(),
                "refresh",
                List.of(UserRole.CUSTOMER.name()),
                ISSUED_AT.plus(Duration.ofMinutes(15))
        ));
        assertInvalid(token(
                UUID.randomUUID().toString(),
                AccessTokenClaimsValidator.ACCESS_TOKEN_USE,
                List.of("SUPER_ADMIN"),
                ISSUED_AT.plus(Duration.ofMinutes(15))
        ));
        assertInvalid(token(
                UUID.randomUUID().toString(),
                AccessTokenClaimsValidator.ACCESS_TOKEN_USE,
                List.of(UserRole.CUSTOMER.name(), UserRole.ADMIN.name()),
                ISSUED_AT.plus(Duration.ofMinutes(15))
        ));
        assertInvalid(token(
                UUID.randomUUID().toString(),
                List.of(AccessTokenClaimsValidator.ACCESS_TOKEN_USE),
                List.of(UserRole.CUSTOMER.name()),
                ISSUED_AT.plus(Duration.ofMinutes(15))
        ));
        assertInvalid(token(
                UUID.randomUUID().toString(),
                AccessTokenClaimsValidator.ACCESS_TOKEN_USE,
                UserRole.CUSTOMER.name(),
                ISSUED_AT.plus(Duration.ofMinutes(15))
        ));
        assertInvalid(token(
                UUID.randomUUID().toString(),
                AccessTokenClaimsValidator.ACCESS_TOKEN_USE,
                List.of(1),
                ISSUED_AT.plus(Duration.ofMinutes(15))
        ));
        assertInvalid(token(
                UUID.randomUUID().toString(),
                AccessTokenClaimsValidator.ACCESS_TOKEN_USE,
                List.of(UserRole.CUSTOMER.name()),
                ISSUED_AT.plus(Duration.ofMinutes(30)).plusNanos(1)
        ));
    }

    private static Jwt token(
            String subject,
            Object tokenUse,
            Object roles,
            Instant expiresAt
    ) {
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .issuer("https://api.lyrashop.test")
                .audience(List.of("lyrashop-api"))
                .subject(subject)
                .issuedAt(ISSUED_AT)
                .expiresAt(expiresAt)
                .claim("jti", UUID.randomUUID().toString())
                .claim(AccessTokenClaimsValidator.TOKEN_USE_CLAIM, tokenUse)
                .claim(AccessTokenClaimsValidator.ROLES_CLAIM, roles)
                .build();
    }

    private static void assertInvalid(Jwt token) {
        assertThat(VALIDATOR.validate(token).hasErrors()).isTrue();
    }
}
