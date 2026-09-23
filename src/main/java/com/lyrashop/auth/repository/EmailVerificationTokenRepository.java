package com.lyrashop.auth.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lyrashop.auth.entity.EmailVerificationToken;

import jakarta.persistence.LockModeType;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    boolean existsByUserIdAndCreatedAtAfter(UUID userId, Instant createdAfter);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from EmailVerificationToken token where token.tokenHash=:hash and token.usedAt is null and token.expiresAt > CURRENT_TIMESTAMP")
    Optional<EmailVerificationToken> findActiveForUpdate(@Param("hash") byte[] hash);

    @Modifying
    @Query("update EmailVerificationToken token set token.usedAt=CURRENT_TIMESTAMP where token.userId=:userId and token.usedAt is null")
    int invalidateAll(@Param("userId") UUID userId);
}
