package com.sprint.deokhugam.review.application.port.out;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sprint.deokhugam.review.domain.ReviewLike;

public interface ReviewLikeRepositoryPort extends JpaRepository<ReviewLike, UUID>, ReviewLikeRepositoryCustom {

    boolean existsByReviewIdAndUserId(UUID reviewId, UUID userId);

    Optional<ReviewLike> findByReviewIdAndUserId(UUID reviewId, UUID requestUserId);
}
