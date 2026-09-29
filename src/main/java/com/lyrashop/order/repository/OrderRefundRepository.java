package com.lyrashop.order.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lyrashop.order.entity.OrderRefund;

public interface OrderRefundRepository extends JpaRepository<OrderRefund, UUID> {
    List<OrderRefund> findAllByOrderIdOrderByCreatedAtDesc(UUID orderId);
    boolean existsByReference(String reference);

    @Query("select coalesce(sum(refund.amount), 0) from OrderRefund refund where refund.orderId = :orderId")
    BigDecimal sumAmountByOrderId(@Param("orderId") UUID orderId);
}
