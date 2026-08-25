package com.lyrashop.catalog.category.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lyrashop.catalog.category.dto.CategoryResponse;
import com.lyrashop.catalog.category.dto.CreateCategoryRequest;
import com.lyrashop.catalog.category.dto.UpdateCategoryRequest;
import com.lyrashop.catalog.category.service.CategoryService;
import com.lyrashop.exception.CategoryNotFoundException;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<CategoryResponse> create(
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CategoryResponse.from(categoryService.create(request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(
            path = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<CategoryResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        return ResponseEntity.ok(CategoryResponse.from(categoryService.update(categoryId(id), request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(path = "/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable String id) {
        categoryService.deactivate(categoryId(id));
        return ResponseEntity.noContent().build();
    }

    private static Long categoryId(String id) {
        try {
            long parsed = Long.parseLong(id);
            if (parsed <= 0) {
                throw new CategoryNotFoundException();
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new CategoryNotFoundException();
        }
    }
}
