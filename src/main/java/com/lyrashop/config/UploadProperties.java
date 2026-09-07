package com.lyrashop.config;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "app.uploads")
public record UploadProperties(
        @NotBlank String directory,
        @Min(1024) @Max(10_485_760) int maxBytes
) {
    public Path directoryPath() {
        return Path.of(directory).toAbsolutePath().normalize();
    }
}
