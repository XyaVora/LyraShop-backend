package com.lyrashop.messaging.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lyrashop.messaging.entity.EmailOutbox;

import jakarta.persistence.LockModeType;

public interface EmailOutboxRepository extends JpaRepository<EmailOutbox, Long> {
    @Query("select e.id from EmailOutbox e where e.status = 'PENDING' and e.nextAttemptAt <= :now order by e.nextAttemptAt, e.id")
    List<Long> findDueIds(@Param("now") Instant now, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EmailOutbox e where e.id = :id and e.status = 'PENDING'")
    Optional<EmailOutbox> findPendingForUpdate(@Param("id") Long id);
}
