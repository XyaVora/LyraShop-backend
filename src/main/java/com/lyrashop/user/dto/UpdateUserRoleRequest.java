package com.lyrashop.user.dto;

import com.lyrashop.user.entity.UserRole;

import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull UserRole role
) {
}
