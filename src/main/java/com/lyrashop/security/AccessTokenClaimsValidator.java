package com.lyrashop.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import com.lyrashop.user.entity.UserRole;

public final class AccessTokenClaimsValidator implements OAuth2TokenValidator<Jwt> {

    public static final String ROLES_CLAIM = "roles";
    public static final String TOKEN_USE_CLAIM = "token_use";
    public static final String ACCESS_TOKEN_USE = "access";

    private static final Set<String> ALLOWED_ROLES =
            Set.of(UserRole.CUSTOMER.name(), UserRole.ADMIN.name());
    private static final OAuth2TokenValidatorResult INVALID =
            OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    "invalid_token",
                    "Access token claims are invalid",
                    null
            ));

    private final Duration maximumLifetime;

    public AccessTokenClaimsValidator(Duration maximumLifetime) {
        this.maximumLifetime = maximumLifetime;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        Instant issuedAt = token.getIssuedAt();
        Instant expiresAt = token.getExpiresAt();
        if (issuedAt == null
                || expiresAt == null
                || !expiresAt.isAfter(issuedAt)
                || Duration.between(issuedAt, expiresAt).compareTo(maximumLifetime) > 0
                || isBlank(token.getId())
                || !hasUuidSubject(token)
                || !ACCESS_TOKEN_USE.equals(token.getClaims().get(TOKEN_USE_CLAIM))
                || !hasAllowedRoles(token)) {
            return INVALID;
        }
        return OAuth2TokenValidatorResult.success();
    }

    private static boolean hasUuidSubject(Jwt token) {
        try {
            UUID.fromString(token.getSubject());
            return true;
        } catch (IllegalArgumentException | NullPointerException exception) {
            return false;
        }
    }

    private static boolean hasAllowedRoles(Jwt token) {
        Object rawRoles = token.getClaims().get(ROLES_CLAIM);
        if (!(rawRoles instanceof java.util.List<?> roles)
                || roles.size() != 1
                || !(roles.getFirst() instanceof String role)) {
            return false;
        }
        return ALLOWED_ROLES.contains(role);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
