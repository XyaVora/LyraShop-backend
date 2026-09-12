package com.lyrashop.promotion.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lyrashop.promotion.entity.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    Optional<Promotion> findFirstByActiveTrueAndStartsAtLessThanEqualAndEndsAtGreaterThanOrderByEndsAtAsc(
            Instant startedBefore, Instant endsAfter);
}
