package com.lyrashop.config;

import java.time.Duration;

import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Validated
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        @NotBlank @Size(max = 200) @URL(protocol = "https") String issuer,
        @NotBlank @Size(max = 200) String audience,
        @NotNull
        @DurationMin(minutes = 15)
        @DurationMax(minutes = 30)
        Duration accessTokenTtl,
        @NotBlank @Size(max = 512) String secretBase64
) {

    public JwtProperties {
        issuer = stripNullable(issuer);
        audience = stripNullable(audience);
        secretBase64 = stripNullable(secretBase64);
    }

    private static String stripNullable(String value) {
        return value == null ? null : value.strip();
    }

    @Override
    public String toString() {
        return "JwtProperties[issuer=" + issuer
                + ", audience=" + audience
                + ", accessTokenTtl=" + accessTokenTtl
                + ", secretBase64=[REDACTED]]";
    }
}
