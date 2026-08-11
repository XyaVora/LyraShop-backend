package com.lyrashop.exception;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        Map<String, String> fieldErrors
) {

    public ApiErrorResponse {
        fieldErrors = fieldErrors == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(fieldErrors));
    }

    public static ApiErrorResponse of(
            int status,
            String code,
            String message,
            String path
    ) {
        return new ApiErrorResponse(Instant.now(), status, code, message, path, Map.of());
    }

    public static ApiErrorResponse withFieldErrors(
            int status,
            String code,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
        return new ApiErrorResponse(Instant.now(), status, code, message, path, fieldErrors);
    }
}
