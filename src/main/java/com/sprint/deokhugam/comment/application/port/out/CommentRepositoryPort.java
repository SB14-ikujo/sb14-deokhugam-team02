package com.sprint.deokhugam.comment.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sprint.deokhugam.comment.domain.Comment;

public interface CommentRepositoryPort extends JpaRepository<Comment, UUID> {

    Optional<Comment> findByIdAndIsDeletedFalse(UUID id);

    // 커서 페이지네이션: 첫 페이지 (최신순)
    @Query("""
        select c from Comment c
        join fetch c.user
        where c.review.id = :reviewId
          and c.isDeleted = false
        order by c.createdAt desc, c.id desc
        """)
    List<Comment> findFirstPage(
        @Param("reviewId") UUID reviewId,
        Pageable pageable
    );

    // 커서 페이지네이션: 다음 페이지 (after 이전에 생성된 댓글)
    @Query("""
        select c from Comment c
        join fetch c.user
        where c.review.id = :reviewId
          and c.isDeleted = false
          and (c.createdAt < :after
               or (c.createdAt = :after and c.id < :afterId))
                       order by c.createdAt desc, c.id desc
        """)
    List<Comment> findNextPage(
        @Param("reviewId") UUID reviewId,
        @Param("after") Instant after,
        @Param("afterId") UUID afterId,
        Pageable pageable
    );

    long countByReviewIdAndIsDeletedFalse(UUID reviewId);
}
