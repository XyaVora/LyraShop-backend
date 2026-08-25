package com.lyrashop.catalog.category.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lyrashop.catalog.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByActiveTrueOrderByNameAscIdAsc();

    Optional<Category> findByIdAndActiveTrue(Long id);

    Optional<Category> findBySlugAndActiveTrue(String slug);

    boolean existsByIdAndActiveTrue(Long id);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
