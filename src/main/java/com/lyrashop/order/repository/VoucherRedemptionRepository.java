package com.lyrashop.order.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lyrashop.order.entity.VoucherRedemption;

public interface VoucherRedemptionRepository extends JpaRepository<VoucherRedemption, Long> {
    long countByVoucherId(UUID voucherId);
    long countByVoucherIdAndUserId(UUID voucherId, UUID userId);
    void deleteByOrderId(UUID orderId);
}
