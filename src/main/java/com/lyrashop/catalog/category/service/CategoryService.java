package com.lyrashop.catalog.category.service;

import java.sql.SQLException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.lyrashop.catalog.category.dto.CreateCategoryRequest;
import com.lyrashop.catalog.category.dto.UpdateCategoryRequest;
import com.lyrashop.catalog.category.entity.Category;
import com.lyrashop.catalog.category.repository.CategoryRepository;
import com.lyrashop.catalog.product.repository.ProductRepository;
import com.lyrashop.exception.CategoryNotFoundException;
import com.lyrashop.exception.CategorySlugAlreadyExistsException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class CategoryService {

    private static final String SLUG_UNIQUE_CONSTRAINT = "uk_categories_slug";
    private static final int MYSQL_DUPLICATE_KEY_ERROR = 1062;

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResult> listActive() {
        return categoryRepository.findAllByActiveTrueOrderByNameAscIdAsc()
                .stream()
                .map(CategoryResult::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResult getActive(@NotNull Long id) {
        if (id <= 0) {
            throw new CategoryNotFoundException();
        }
        return categoryRepository.findByIdAndActiveTrue(id)
                .map(CategoryResult::from)
                .orElseThrow(CategoryNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public long countActiveProducts(Long categoryId) {
        return productRepository.countByCategoryIdAndActiveTrue(categoryId);
    }

    @Transactional
    public CategoryResult create(@NotNull @Valid CreateCategoryRequest request) {
        String slug = Category.normalizeSlug(request.slug());
        if (request.parentId() != null
                && !categoryRepository.existsByIdAndActiveTrue(request.parentId())) {
            throw new CategoryNotFoundException();
        }
        if (categoryRepository.existsBySlug(slug)) {
            throw new CategorySlugAlreadyExistsException();
        }

        Category category = Category.create(
                request.name(),
                slug,
                request.description(),
                request.parentId()
        );
        try {
            return CategoryResult.from(categoryRepository.saveAndFlush(category));
        } catch (DataIntegrityViolationException exception) {
            if (isSlugUniqueViolation(exception)) {
                throw new CategorySlugAlreadyExistsException(exception);
            }
            throw exception;
        }
    }

    @Transactional
    public CategoryResult update(@NotNull Long id, @NotNull @Valid UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(CategoryNotFoundException::new);
        if (request.parentId() != null) {
            if (request.parentId().equals(id)
                    || !categoryRepository.existsByIdAndActiveTrue(request.parentId())) {
                throw new CategoryNotFoundException();
            }
        }
        String slug = Category.normalizeSlug(request.slug());
        if (categoryRepository.existsBySlugAndIdNot(slug, id)) {
            throw new CategorySlugAlreadyExistsException();
        }
        category.update(request.name(), slug, request.description(), request.parentId());
        try {
            return CategoryResult.from(categoryRepository.saveAndFlush(category));
        } catch (DataIntegrityViolationException exception) {
            if (isSlugUniqueViolation(exception)) {
                throw new CategorySlugAlreadyExistsException(exception);
            }
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<CategoryResult> listForAdmin() {
        return categoryRepository.findAllByOrderByNameAscIdAsc()
                .stream()
                .map(CategoryResult::from)
                .toList();
    }

    @Transactional
    public void deactivate(@NotNull Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(CategoryNotFoundException::new);
        if (!category.isActive()) {
            return;
        }
        category.deactivate();
        categoryRepository.saveAndFlush(category);
    }

    @Transactional
    public void activate(@NotNull Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(CategoryNotFoundException::new);
        if (category.isActive()) {
            return;
        }
        category.activate();
        categoryRepository.saveAndFlush(category);
    }

    private static boolean isSlugUniqueViolation(Throwable failure) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable current = failure;

        while (current != null && visited.add(current)) {
            if (current instanceof ConstraintViolationException violation
                    && violation.getKind() == ConstraintViolationException.ConstraintKind.UNIQUE
                    && isSlugConstraint(violation.getConstraintName())) {
                return true;
            }
            if (current instanceof SQLException sqlException
                    && sqlException.getErrorCode() == MYSQL_DUPLICATE_KEY_ERROR
                    && "23000".equals(sqlException.getSQLState())
                    && containsSlugConstraint(sqlException.getMessage())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static boolean isSlugConstraint(String constraintName) {
        if (constraintName == null) {
            return false;
        }
        String normalized = constraintName
                .replace(String.valueOf((char) 96), "")
                .replace(String.valueOf((char) 34), "")
                .toLowerCase(Locale.ROOT);
        return normalized.equals(SLUG_UNIQUE_CONSTRAINT)
                || normalized.endsWith("." + SLUG_UNIQUE_CONSTRAINT);
    }

    private static boolean containsSlugConstraint(String message) {
        return message != null
                && message.toLowerCase(Locale.ROOT).contains(SLUG_UNIQUE_CONSTRAINT);
    }
}
