package com.lyrashop.auth.repository;

import java.time.Instant;
import java.util.UUID;

public record RefreshSessionSnapshot(
        UUID id,
        UUID userId,
        UUID familyId,
        Instant expiresAt,
        Instant consumedAt,
        Instant revokedAt,
        boolean userActive
) {
}
