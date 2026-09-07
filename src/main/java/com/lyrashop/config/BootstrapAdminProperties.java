package com.lyrashop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.bootstrap.admin")
public record BootstrapAdminProperties(
        String email,
        String password,
        String fullName
) {

    public BootstrapAdminProperties {
        email = blankToNull(email);
        password = blankToNull(password);
        fullName = blankToNull(fullName);
    }

    public boolean isConfigured() {
        return email != null && password != null;
    }

    public boolean isPartiallyConfigured() {
        return (email == null) != (password == null);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip();
    }
}
