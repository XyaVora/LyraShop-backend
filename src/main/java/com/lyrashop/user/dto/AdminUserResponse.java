package com.lyrashop.user.dto;

import java.time.Instant;
import java.util.UUID;

import com.lyrashop.user.entity.User;

public record AdminUserResponse(
        UUID id,
        String email,
        String fullName,
        String phone,
        String role,
        boolean active,
        Instant createdAt
) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
