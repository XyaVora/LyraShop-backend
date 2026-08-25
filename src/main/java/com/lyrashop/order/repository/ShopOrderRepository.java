package com.lyrashop.order.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.lyrashop.order.entity.ShopOrder;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, UUID> {

    List<ShopOrder> findAllByUserIdOrderByCreatedAtDescIdDesc(UUID userId);

    List<ShopOrder> findAllByOrderByCreatedAtDescIdDesc();

    Optional<ShopOrder> findByIdAndUserId(UUID id, UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select shopOrder from ShopOrder shopOrder where shopOrder.id = :id")
    Optional<ShopOrder> findForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select shopOrder from ShopOrder shopOrder where shopOrder.id = :id and shopOrder.userId = :userId")
    Optional<ShopOrder> findForUpdateByUser(@Param("id") UUID id, @Param("userId") UUID userId);
}
