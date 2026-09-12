package com.lyrashop.address.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lyrashop.address.entity.ShippingAddress;

public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, UUID> {
    List<ShippingAddress> findAllByUserIdOrderByDefaultAddressDescCreatedAtAsc(UUID userId);
    Optional<ShippingAddress> findByIdAndUserId(UUID id, UUID userId);
    long countByUserId(UUID userId);

    @Modifying
    @Query("update ShippingAddress a set a.defaultAddress = false where a.userId = :userId and a.defaultAddress = true")
    int clearDefault(@Param("userId") UUID userId);
}
