package com.lyrashop.order.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lyrashop.order.entity.CustomerReturnRequest;

import jakarta.persistence.LockModeType;

public interface CustomerReturnRequestRepository extends JpaRepository<CustomerReturnRequest, UUID>, JpaSpecificationExecutor<CustomerReturnRequest> {
    Optional<CustomerReturnRequest> findByOrderIdAndUserId(UUID orderId, UUID userId);
    Optional<CustomerReturnRequest> findByOrderId(UUID orderId);
    List<CustomerReturnRequest> findAllByOrderByCreatedAtDesc();
    boolean existsByOrderId(UUID orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select request from CustomerReturnRequest request where request.orderId = :orderId")
    Optional<CustomerReturnRequest> findForUpdateByOrderId(@Param("orderId") UUID orderId);
}
