package com.sprint.deokhugam.comment.application.port.out;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sprint.deokhugam.comment.domain.Comment;

public interface CommentRepositoryPort
    extends JpaRepository<Comment, UUID>, CommentRepositoryCustom {

    Optional<Comment> findByIdAndIsDeletedFalse(UUID id);

    long countByReviewIdAndIsDeletedFalse(UUID reviewId);
}
