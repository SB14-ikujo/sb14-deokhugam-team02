package com.sprint.deokhugam.review.application.port.out;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sprint.deokhugam.review.domain.ReviewLike;

public interface ReviewLikeRepositoryPort extends JpaRepository<ReviewLike, UUID> {
    // 해당 리뷰에 사용자가 리뷰를 남겼는가 판단함
    boolean existsByReviewIdAndUserId(UUID reviewId, UUID userId);
}
