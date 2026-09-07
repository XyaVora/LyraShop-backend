package com.lyrashop.user.dto;

import java.time.Instant;
import java.util.UUID;

import com.lyrashop.user.entity.User;

public record ProfileResponse(
        UUID id,
        String email,
        String fullName,
        String phone,
        String role,
        Instant createdAt
) {
    public static ProfileResponse from(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
