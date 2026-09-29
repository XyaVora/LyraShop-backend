package com.lyrashop.catalog.product.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import com.lyrashop.catalog.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    boolean existsBySlug(String slug);

    boolean existsByIdAndActiveTrue(UUID id);

    long countByCategoryIdAndActiveTrue(Long categoryId);


    boolean existsBySlugAndIdNot(String slug, UUID id);

    List<Product> findAllByOrderByCreatedAtDescIdDesc();

    @Query("""
            select product from Product product
            where lower(product.name) like lower(concat('%', :query, '%'))
               or lower(product.slug) like lower(concat('%', :query, '%'))
            order by product.updatedAt desc, product.id desc
            """)
    List<Product> searchForAdmin(@Param("query") String query, Pageable pageable);
}
