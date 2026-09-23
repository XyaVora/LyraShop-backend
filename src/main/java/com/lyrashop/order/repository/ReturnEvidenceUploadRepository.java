package com.lyrashop.order.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lyrashop.order.entity.ReturnEvidenceUpload;

import jakarta.persistence.LockModeType;

public interface ReturnEvidenceUploadRepository extends JpaRepository<ReturnEvidenceUpload, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select upload from ReturnEvidenceUpload upload where upload.url=:url and upload.userId=:userId and upload.consumedAt is null and upload.expiresAt > :now")
    Optional<ReturnEvidenceUpload> findConsumable(@Param("url") String url,@Param("userId") UUID userId,@Param("now") Instant now);
    Optional<ReturnEvidenceUpload> findByUrlAndUserId(String url,UUID userId);

    @Query("select upload from ReturnEvidenceUpload upload where upload.consumedAt is null and upload.expiresAt <= :now order by upload.expiresAt")
    List<ReturnEvidenceUpload> findExpired(@Param("now") Instant now, Pageable pageable);
}
