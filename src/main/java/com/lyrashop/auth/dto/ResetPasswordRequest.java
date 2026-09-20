package com.lyrashop.auth.dto;
import com.lyrashop.auth.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
public record ResetPasswordRequest(@NotBlank String token, @NotBlank @ValidPassword String newPassword) {}
