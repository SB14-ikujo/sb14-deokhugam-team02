package com.sprint.deokhugam.review.application.port.out;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.sprint.deokhugam.review.domain.Review;

public interface ReviewCommandRepositoryPort extends ReviewRepositoryPort, JpaRepository<Review, UUID> {
    // 리뷰의 '좋아요 수' 증가
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Review r SET r.likeCount = r.likeCount + 1 WHERE r.id = :reviewId")
    void increaseLikeCount(UUID reviewId);

    // 리뷰의 '좋아요 수' 감소
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Review r SET r.likeCount = r.likeCount - 1 WHERE r.id = :reviewId")
    void decreaseLikeCount(UUID reviewId);
}
