package com.lyrashop.auth.repository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import com.lyrashop.auth.entity.PasswordResetToken;
import jakarta.persistence.LockModeType;
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
 boolean existsByUserIdAndCreatedAtAfter(UUID userId, java.time.Instant createdAfter);
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select t from PasswordResetToken t where t.tokenHash=:hash and t.usedAt is null and t.expiresAt > CURRENT_TIMESTAMP")
 Optional<PasswordResetToken> findActiveForUpdate(@Param("hash") byte[] hash);
 @Modifying @Query("update PasswordResetToken t set t.usedAt=CURRENT_TIMESTAMP where t.userId=:userId and t.usedAt is null")
 int invalidateAll(@Param("userId") UUID userId);
}
