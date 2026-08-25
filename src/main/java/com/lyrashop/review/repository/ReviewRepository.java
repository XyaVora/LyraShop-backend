package com.lyrashop.review.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lyrashop.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findAllByProductIdOrderByCreatedAtDescIdDesc(UUID productId);

    List<Review> findAllByOrderByCreatedAtDescIdDesc();

    boolean existsByProductIdAndUserId(UUID productId, UUID userId);
}
