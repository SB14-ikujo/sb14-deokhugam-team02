package com.sprint.deokhugam.comment.adapter.in.web.dto;

import java.util.UUID;

public record CommentCreateRequest(
    UUID reviewId,
    UUID userId,
    String content
) {
}
