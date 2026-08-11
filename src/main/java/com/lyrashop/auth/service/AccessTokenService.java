package com.lyrashop.auth.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import com.lyrashop.config.JwtProperties;
import com.lyrashop.security.AccessTokenClaimsValidator;
import com.lyrashop.user.entity.User;

@Service
public class AccessTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;
    private final Clock clock;

    public AccessTokenService(
            JwtEncoder jwtEncoder,
            JwtProperties properties,
            Clock jwtClock
    ) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
        this.clock = jwtClock;
    }

    public IssuedAccessToken issue(User user) {
        Objects.requireNonNull(user, "user");
        Objects.requireNonNull(user.getId(), "persisted user id");
        Objects.requireNonNull(user.getRole(), "user role");

        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(properties.accessTokenTtl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(user.getId().toString())
                .audience(List.of(properties.audience()))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim(AccessTokenClaimsValidator.TOKEN_USE_CLAIM,
                        AccessTokenClaimsValidator.ACCESS_TOKEN_USE)
                .claim(AccessTokenClaimsValidator.ROLES_CLAIM,
                        List.of(user.getRole().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
        return new IssuedAccessToken(token, properties.accessTokenTtl().toSeconds());
    }
}
