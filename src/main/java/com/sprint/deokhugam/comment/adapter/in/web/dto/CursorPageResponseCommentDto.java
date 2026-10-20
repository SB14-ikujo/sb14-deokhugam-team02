package com.sprint.deokhugam.comment.adapter.in.web.dto;

import java.time.Instant;
import java.util.List;

public record CursorPageResponseCommentDto(
    List<CommentDto> content,
    Instant nextAfter,
    int size,
    boolean hasNext
) {
}
