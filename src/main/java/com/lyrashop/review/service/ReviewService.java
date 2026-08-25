package com.lyrashop.review.service;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return reviews.findAllByProductIdOrderByCreatedAtDescIdDesc(productId).stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> listAll() {
        return reviews.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Transactional
    public void delete(Long reviewId) {
        if (!reviews.existsById(reviewId)) {
            throw new ReviewNotFoundException();
        }
        reviews.deleteById(reviewId);
    }
}
