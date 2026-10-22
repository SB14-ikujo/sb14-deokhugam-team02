package com.sprint.deokhugam.comment.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentDto(
    UUID id,
    UUID reviewId,
    UUID userId,
    String userNickname,
    String content,
    Instant createdAt,
    Instant updatedAt
) {
}
