package com.lyrashop.auth.dto;

import com.lyrashop.auth.validation.ValidPassword;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @ValidPassword String newPassword
) {}
