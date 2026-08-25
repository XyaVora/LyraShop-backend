package com.lyrashop.review.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lyrashop.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findAllByProductIdOrderByCreatedAtDescIdDesc(UUID productId);

    List<Review> findAllByOrderByCreatedAtDescIdDesc();

    boolean existsByProductIdAndUserId(UUID productId, UUID userId);

    @Query("""
            select review.productId as productId,
                   avg(review.rating) as averageRating,
                   count(review) as reviewCount
            from Review review
            where review.productId in :productIds
            group by review.productId
            """)
    List<ProductReviewSummary> summarizeByProductIds(@Param("productIds") Collection<UUID> productIds);
}
