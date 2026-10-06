package com.lyrashop.catalog.category.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import com.lyrashop.common.dto.PageResponse;
import com.lyrashop.common.web.AdminPageable;

import com.lyrashop.catalog.category.dto.AdminCategoryResponse;
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

    @PreAuthorize("hasAnyRole('ADMIN','CATALOG_MANAGER')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AdminCategoryResponse> list() {
        return categoryService.listForAdmin().stream().map(AdminCategoryResponse::from).toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','CATALOG_MANAGER')")
    @GetMapping(path = "/page", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<AdminCategoryResponse> page(@RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size, @RequestParam(required=false) String query,
            @RequestParam(required=false) Boolean active, @RequestParam(defaultValue="name") String sort,
            @RequestParam(defaultValue="asc") String direction) {
        var result = categoryService.pageForAdmin(query, active, AdminPageable.of(page,size,sort,direction,
                java.util.Set.of("name","slug","parentId","active","createdAt","updatedAt"),"name"));
        var content = result.getContent().stream().map(AdminCategoryResponse::from).toList();
        return PageResponse.from(result, content);
    }

    @PreAuthorize("hasAnyRole('ADMIN','CATALOG_MANAGER')")
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

    @PreAuthorize("hasAnyRole('ADMIN','CATALOG_MANAGER')")
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

    @PreAuthorize("hasAnyRole('ADMIN','CATALOG_MANAGER')")
    @PatchMapping(path = "/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable String id) {
        categoryService.deactivate(categoryId(id));
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','CATALOG_MANAGER')")
    @PatchMapping(path = "/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable String id) {
        categoryService.activate(categoryId(id));
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
