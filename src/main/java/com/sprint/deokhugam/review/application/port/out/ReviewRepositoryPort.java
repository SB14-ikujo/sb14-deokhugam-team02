package com.sprint.deokhugam.review.application.port.out;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sprint.deokhugam.review.domain.Review;

public interface ReviewRepositoryPort
    extends JpaRepository<Review, UUID>, ReviewRepositoryCustom, ReviewCommandRepositoryPort {

    Optional<Review> findByIdAndIsDeletedFalse(UUID reviewId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Review r
            SET r.commentCount = r.commentCount + 1
            WHERE r.id = :reviewId
        """)
    int addCommentCount(@Param("reviewId") UUID reviewId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Review r
            SET r.commentCount = CASE
                    WHEN r.commentCount <= 1 THEN 0
                    ELSE r.commentCount - 1
                END
            WHERE r.id = :reviewId
        """)
    int removeCommentCount(@Param("reviewId") UUID reviewId);
}
