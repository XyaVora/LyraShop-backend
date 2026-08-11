package com.lyrashop.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.lyrashop.auth.entity.RefreshSession;
import com.lyrashop.auth.entity.RefreshTokenDigest;

public interface RefreshSessionRepository extends JpaRepository<RefreshSession, UUID> {

    Optional<RefreshSession> findByTokenHash(byte[] tokenHash);

    default Optional<RefreshSession> findByTokenDigest(RefreshTokenDigest tokenDigest) {
        return findByTokenHash(tokenDigest.bytes());
    }

    @Query("""
            select session
            from RefreshSession session
            where session.tokenHash = :tokenHash
              and session.consumedAt is null
              and session.revokedAt is null
              and session.expiresAt > CURRENT_TIMESTAMP
              and session.user.active = true
            """)
    Optional<RefreshSession> findActiveByTokenHash(@Param("tokenHash") byte[] tokenHash);

    default Optional<RefreshSession> findActiveByTokenDigest(RefreshTokenDigest tokenDigest) {
        return findActiveByTokenHash(tokenDigest.bytes());
    }

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update RefreshSession session
            set session.consumedAt = CURRENT_TIMESTAMP,
                session.version = session.version + 1
            where session.id = :id
              and session.consumedAt is null
              and session.revokedAt is null
              and session.expiresAt > CURRENT_TIMESTAMP
              and session.user.id in (
                  select user.id
                  from User user
                  where user.active = true
              )
            """)
    int consumeIfActive(@Param("id") UUID id);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update RefreshSession session
            set session.revokedAt = CURRENT_TIMESTAMP,
                session.version = session.version + 1
            where session.familyId = :familyId
              and session.revokedAt is null
            """)
    int revokeFamilyIfActive(@Param("familyId") UUID familyId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update RefreshSession session
            set session.revokedAt = CURRENT_TIMESTAMP,
                session.version = session.version + 1
            where session.user.id = :userId
              and session.revokedAt is null
            """)
    int revokeAllByUserId(@Param("userId") UUID userId);
}
