package com.lyrashop.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "must not be blank")
        @Email(message = "must be a well-formed email address")
        @Size(max = 255, message = "must not exceed 255 characters")
        String email,

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @NotBlank(message = "must not be blank")
        String password
) {

    public LoginRequest {
        email = email == null ? null : email.strip();
    }

    @Override
    public String toString() {
        return "LoginRequest[password=[REDACTED]]";
    }
}
