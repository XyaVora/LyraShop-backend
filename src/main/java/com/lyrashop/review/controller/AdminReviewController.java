package com.lyrashop.review.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.Authentication;

import com.lyrashop.review.dto.ReviewResponse;
import com.lyrashop.review.service.ReviewService;
import com.lyrashop.common.dto.PageResponse;
import com.lyrashop.common.web.AdminPageable;
import com.lyrashop.review.dto.ModerateReviewRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','CATALOG_MANAGER','SUPPORT')")
    @GetMapping
    public List<ReviewResponse> list() {
        return reviewService.listAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN','CATALOG_MANAGER','SUPPORT')")
    @GetMapping("/page")
    public PageResponse<ReviewResponse> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String moderationStatus,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Integer safeRating = rating != null && rating >= 1 && rating <= 5 ? rating : null;
        String safeStatus = "PUBLISHED".equals(moderationStatus) || "HIDDEN".equals(moderationStatus) ? moderationStatus : null;
        var result = reviewService.pageAll(query, safeRating, safeStatus, AdminPageable.of(
                page, size, sort, direction,
                java.util.Set.of("id", "rating", "comment", "createdAt"), "createdAt"));
        return PageResponse.from(result, result.getContent());
    }

    @PreAuthorize("hasAnyRole('ADMIN','CATALOG_MANAGER','SUPPORT')")
    @PutMapping("/{id}/moderation")
    public ReviewResponse moderate(Authentication authentication, @PathVariable Long id,
            @Valid @RequestBody ModerateReviewRequest request) {
        return reviewService.moderate(id, request.status(), request.note(), UUID.fromString(authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CATALOG_MANAGER','SUPPORT')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
