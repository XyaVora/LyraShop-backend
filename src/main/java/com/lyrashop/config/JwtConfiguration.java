package com.lyrashop.config;

import java.time.Clock;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtAudienceValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuedAtValidator;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import com.lyrashop.security.AccessTokenClaimsValidator;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfiguration {

    static final int MINIMUM_SECRET_BYTES = 32;
    static final Duration CLOCK_SKEW = Duration.ofSeconds(30);
    static final Duration MAXIMUM_ACCESS_TOKEN_LIFETIME = Duration.ofMinutes(30);

    @Bean
    Clock jwtClock() {
        return Clock.systemUTC();
    }

    @Bean
    SecretKey jwtSecretKey(JwtProperties properties) {
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(properties.secretBase64());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT signing secret must be valid Base64", exception);
        }
        if (decoded.length < MINIMUM_SECRET_BYTES) {
            Arrays.fill(decoded, (byte) 0);
            throw new IllegalStateException(
                    "JWT signing secret must decode to at least " + MINIMUM_SECRET_BYTES + " bytes"
            );
        }

        try {
            return new SecretKeySpec(decoded, "HmacSHA256");
        } finally {
            Arrays.fill(decoded, (byte) 0);
        }
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey secretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    @Bean
    JwtDecoder jwtDecoder(
            SecretKey secretKey,
            JwtProperties properties,
            Clock jwtClock
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        JwtTimestampValidator timestampValidator = new JwtTimestampValidator(CLOCK_SKEW);
        timestampValidator.setClock(jwtClock);
        JwtIssuedAtValidator issuedAtValidator = new JwtIssuedAtValidator();
        issuedAtValidator.setClock(jwtClock);
        issuedAtValidator.setClockSkew(CLOCK_SKEW);

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                timestampValidator,
                issuedAtValidator,
                new JwtIssuerValidator(properties.issuer()),
                new JwtAudienceValidator(properties.audience()),
                new AccessTokenClaimsValidator(MAXIMUM_ACCESS_TOKEN_LIFETIME)
        ));
        return decoder;
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName(AccessTokenClaimsValidator.ROLES_CLAIM);
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter authenticationConverter =
                new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return authenticationConverter;
    }
}
