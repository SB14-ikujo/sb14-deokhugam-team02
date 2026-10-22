package com.sprint.deokhugam.review.application.port.out;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sprint.deokhugam.review.domain.Review;

public interface ReviewRepositoryPort extends JpaRepository<Review, UUID>, ReviewRepositoryCustom {

    @Query("SELECT r "
        + " FROM Review r "
        + " WHERE r.user.id = :userId "
        + " AND r.book.id = :bookId "
        + " AND r.isDeleted = false")
    Optional<Review> findValidReview(@Param("userId") UUID userId, @Param("bookId") UUID bookId);

    Optional<Review> findByIdAndIsDeletedFalse(UUID reviewId);

    @Query("SELECT r "
        + "FROM Review r "
        + "JOIN FETCH r.user "
        + "JOIN FETCH r.book "
        + "WHERE r.id = :reviewId "
        + "AND r.isDeleted = false")
    Optional<Review> findByFetchJoin(@Param("reviewId") UUID reviewId);

    void increaseLikeCount(UUID reviewId);

    void decreaseLikeCount(UUID reviewId);
}
