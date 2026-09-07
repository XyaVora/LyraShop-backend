package com.lyrashop.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = 255, message = "must not exceed 255 characters")
        String fullName,

        @Size(max = 20, message = "must not exceed 20 characters")
        String phone
) {
    public UpdateProfileRequest {
        fullName = stripNullable(fullName);
        phone = stripNullable(phone);
    }

    private static String stripNullable(String value) {
        return value == null ? null : value.strip();
    }
}
