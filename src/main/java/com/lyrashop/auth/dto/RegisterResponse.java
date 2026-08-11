package com.lyrashop.auth.dto;

import java.time.Instant;
import java.util.UUID;

import com.lyrashop.auth.service.RegistrationResult;

public record RegisterResponse(
        UUID id,
        String email,
        String fullName,
        String phone,
        Instant createdAt
) {

    public static RegisterResponse from(RegistrationResult result) {
        return new RegisterResponse(
                result.id(),
                result.email(),
                result.fullName(),
                result.phone(),
                result.createdAt()
        );
    }
}
