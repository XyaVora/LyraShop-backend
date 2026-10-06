package com.lyrashop.review.service;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.lyrashop.catalog.product.repository.ProductRepository;
import com.lyrashop.catalog.product.service.ProductNotFoundException;
import com.lyrashop.order.repository.ShopOrderRepository;
import com.lyrashop.review.dto.CreateReviewRequest;
import com.lyrashop.review.dto.ReviewResponse;
import com.lyrashop.review.entity.Review;
import com.lyrashop.review.repository.ReviewRepository;

@Service
public class ReviewService {

    private final ReviewRepository reviews;
    private final ProductRepository products;
    private final ShopOrderRepository orders;

    public ReviewService(
            ReviewRepository reviews,
            ProductRepository products,
            ShopOrderRepository orders
    ) {
        this.reviews = reviews;
        this.products = products;
        this.orders = orders;
    }

    @Transactional
    public ReviewResponse create(UUID userId, UUID productId, CreateReviewRequest request) {
        if (!products.existsByIdAndActiveTrue(productId)) {
            throw new ProductNotFoundException();
        }
        if (!orders.hasDeliveredProduct(userId, productId)) {
            throw new ReviewNotAllowedException();
        }
        if (reviews.existsByProductIdAndUserId(productId, userId)) {
            throw new ReviewAlreadyExistsException();
        }
        try {
            return ReviewResponse.from(reviews.saveAndFlush(
                    Review.create(productId, userId, request.rating(), request.comment())
            ));
        } catch (DataIntegrityViolationException exception) {
            throw new ReviewAlreadyExistsException();
        }
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> listForProduct(UUID productId) {
        if (!products.existsByIdAndActiveTrue(productId)) {
            throw new ProductNotFoundException();
        }
        return reviews.findAllByProductIdAndModerationStatusOrderByCreatedAtDescIdDesc(productId, "PUBLISHED").stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> listAll() {
        return reviews.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> pageAll(String query, Integer rating, String moderationStatus, Pageable pageable) {
        Specification<Review> specification = (root, ignored, builder) -> builder.conjunction();
        if (query != null && !query.isBlank()) {
            String pattern = "%" + query.strip().toLowerCase(java.util.Locale.ROOT) + "%";
            specification = specification.and((root, ignored, builder) ->
                    builder.like(builder.lower(root.get("comment")), pattern));
        }
        if (rating != null) specification = specification.and((root, ignored, builder) -> builder.equal(root.get("rating"), rating));
        if (moderationStatus != null && !moderationStatus.isBlank()) specification = specification.and((root, ignored, builder) -> builder.equal(root.get("moderationStatus"), moderationStatus));
        return reviews.findAll(specification, pageable).map(ReviewResponse::from);
    }

    @Transactional
    public ReviewResponse moderate(Long reviewId, String status, String note, UUID adminId) {
        Review review = reviews.findById(reviewId).orElseThrow(ReviewNotFoundException::new);
        review.moderate(status, note, adminId);
        return ReviewResponse.from(reviews.saveAndFlush(review));
    }

    @Transactional
    public void delete(Long reviewId) {
        if (!reviews.existsById(reviewId)) {
            throw new ReviewNotFoundException();
        }
        reviews.deleteById(reviewId);
    }
}
