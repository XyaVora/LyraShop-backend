package com.lyrashop.catalog.category.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.lyrashop.catalog.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    List<Category> findAllByActiveTrueOrderByNameAscIdAsc();

    List<Category> findAllByOrderByNameAscIdAsc();

    Optional<Category> findByIdAndActiveTrue(Long id);

    Optional<Category> findBySlugAndActiveTrue(String slug);

    boolean existsByIdAndActiveTrue(Long id);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
