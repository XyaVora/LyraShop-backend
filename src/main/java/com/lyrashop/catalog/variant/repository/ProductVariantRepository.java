package com.lyrashop.catalog.variant.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.lyrashop.catalog.variant.entity.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);

    Optional<ProductVariant> findByIdAndProductId(UUID id, UUID productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select variant
            from ProductVariant variant
            where variant.id = :variantId
              and variant.productId = :productId
            """)
    Optional<ProductVariant> findForDeactivation(
            @Param("productId") UUID productId,
            @Param("variantId") UUID variantId
    );

    List<PublicProductVariantProjection> findAllByProductIdAndActiveTrueOrderBySkuAscIdAsc(UUID productId);

    boolean existsBySkuAndIdNot(String sku, UUID id);
}
