package com.sprint.deokhugam.review.application.port.out;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.review.domain.ReviewLike;
import com.sprint.deokhugam.user.domain.User;

public interface ReviewLikeRepositoryPort extends JpaRepository<ReviewLike, UUID>, ReviewLikeRepositoryCustom {
    boolean existsByReviewIdAndUserId(UUID reviewId, UUID userId);

    // 해당 리뷰와 유저가 포함된 '좋아요' 데이터 조회 (있는지 없는지 판단 유무를 가리기 위해 옵셔널로 씀)
    Optional<ReviewLike> findByReviewAndUser(Review review, User requestUser);
}
