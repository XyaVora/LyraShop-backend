package com.lyrashop.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.cors")
public record CorsProperties(List<String> allowedOrigins) {

    public CorsProperties {
        allowedOrigins = allowedOrigins == null
                ? List.of()
                : allowedOrigins.stream()
                        .map(String::strip)
                        .filter(origin -> !origin.isEmpty())
                        .peek(CorsProperties::rejectWildcard)
                        .distinct()
                        .toList();
    }

    private static void rejectWildcard(String origin) {
        if (origin.contains("*")) {
            throw new IllegalArgumentException("CORS origins must be explicit and cannot contain wildcards");
        }
    }
}
