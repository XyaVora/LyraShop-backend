package com.lyrashop.catalog.product.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lyrashop.catalog.product.entity.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findAllByProductIdOrderBySortOrderAscIdAsc(UUID productId);

    List<ProductImage> findAllByProductIdInOrderByProductIdAscSortOrderAscIdAsc(List<UUID> productIds);

    List<ProductImage> findAllByProductIdAndPrimaryTrue(UUID productId);
}
