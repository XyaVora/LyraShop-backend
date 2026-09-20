package com.lyrashop.catalog.product.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.lyrashop.catalog.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    boolean existsBySlug(String slug);

    boolean existsByIdAndActiveTrue(UUID id);

    long countByCategoryIdAndActiveTrue(Long categoryId);


    boolean existsBySlugAndIdNot(String slug, UUID id);

    List<Product> findAllByOrderByCreatedAtDescIdDesc();
}
