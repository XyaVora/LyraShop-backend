package com.lyrashop.order.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.lyrashop.order.entity.Voucher;
import jakarta.persistence.LockModeType;

public interface VoucherRepository extends JpaRepository<Voucher, UUID> {
    @Query("select v from Voucher v where v.active = true and v.startsAt <= :now and v.endsAt > :now order by v.code")
    List<Voucher> findCurrentlyActive(@Param("now") Instant now);
    @Query("select v from Voucher v where v.code = :code and v.active = true and v.startsAt <= :now and v.endsAt > :now")
    Optional<Voucher> findActiveByCode(@Param("code") String code, @Param("now") Instant now);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Voucher v where v.code = :code and v.active = true and v.startsAt <= :now and v.endsAt > :now")
    Optional<Voucher> findActiveByCodeForUpdate(@Param("code") String code, @Param("now") Instant now);
}
