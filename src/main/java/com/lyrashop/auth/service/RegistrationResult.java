package com.lyrashop.auth.service;

import java.time.Instant;
import java.util.UUID;

import com.lyrashop.user.entity.User;

public record RegistrationResult(
        UUID id,
        String email,
        String fullName,
        String phone,
        Instant createdAt
) {

    static RegistrationResult from(User user) {
        return new RegistrationResult(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getCreatedAt()
        );
    }
}
