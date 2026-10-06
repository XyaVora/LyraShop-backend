package com.lyrashop.promotion.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.lyrashop.promotion.entity.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, UUID>, JpaSpecificationExecutor<Promotion> {
    Optional<Promotion> findFirstByActiveTrueAndStartsAtLessThanEqualAndEndsAtGreaterThanOrderByEndsAtAsc(
            Instant startedBefore, Instant endsAfter);
    List<Promotion> findAllByActiveTrueAndStartsAtLessThanEqualAndEndsAtGreaterThanOrderByEndsAtAsc(
            Instant startedBefore, Instant endsAfter);
}
