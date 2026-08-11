package com.lyrashop.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lyrashop.auth.validation.ValidPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "must not be blank")
        @Email(message = "must be a well-formed email address")
        @Size(max = 255, message = "must not exceed 255 characters")
        String email,

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @NotBlank(message = "must not be blank")
        @ValidPassword
        String password,

        @NotBlank(message = "must not be blank")
        @Size(max = 255, message = "must not exceed 255 characters")
        String fullName,

        @Size(max = 20, message = "must not exceed 20 characters")
        String phone
) {

    public RegisterRequest {
        email = stripNullable(email);
        fullName = stripNullable(fullName);
        phone = stripNullable(phone);
    }

    @Override
    public String toString() {
        return "RegisterRequest[password=[REDACTED]]";
    }

    private static String stripNullable(String value) {
        return value == null ? null : value.strip();
    }
}
