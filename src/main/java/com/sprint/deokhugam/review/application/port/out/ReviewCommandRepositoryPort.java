package com.sprint.deokhugam.review.application.port.out;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import com.sprint.deokhugam.review.domain.Review;

@NoRepositoryBean
public interface ReviewCommandRepositoryPort extends JpaRepository<Review, UUID> {
    @Query("SELECT r "
        + " FROM Review r "
        + " WHERE r.user.id = :userId "
        + " AND r.book.id = :bookId "
        + " AND r.isDeleted = false")
    Optional<Review> findValidReview(@Param("userId") UUID userId, @Param("bookId") UUID bookId);

    @Query("SELECT r "
        + "FROM Review r "
        + "JOIN FETCH r.user "
        + "JOIN FETCH r.book "
        + "WHERE r.id = :reviewId "
        + "AND r.isDeleted = false")
    Optional<Review> findByFetchJoin(@Param("reviewId") UUID reviewId);

    // 리뷰의 '좋아요 수' 증가
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Review r SET r.likeCount = r.likeCount + 1 WHERE r.id = :reviewId")
    void increaseLikeCount(UUID reviewId);

    // 리뷰의 '좋아요 수' 감소
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Review r SET r.likeCount = r.likeCount - 1 WHERE r.id = :reviewId AND r.likeCount > 0")
    void decreaseLikeCount(UUID reviewId);
}
