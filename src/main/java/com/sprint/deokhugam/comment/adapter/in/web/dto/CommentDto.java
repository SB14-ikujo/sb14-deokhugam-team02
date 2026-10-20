package com.sprint.deokhugam.comment.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.sprint.deokhugam.comment.domain.Comment;

public record CommentDto(
    UUID id,
    UUID reviewId,
    UUID userId,
    String userNickname,
    String content,
    Instant createdAt,
    Instant updatedAt
) {
    public static CommentDto from(Comment comment) {
        return new CommentDto(
            comment.getId(),
            comment.getReview().getId(),
            comment.getUser().getId(),
            comment.getUser().getNickname(),
            comment.getContent(),
            comment.getCreatedAt(),
            comment.getUpdatedAt()
        );
    }
}
