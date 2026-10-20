package com.sprint.deokhugam.comment.adapter.in.web.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
    @NotNull UUID reviewId,
    @NotNull UUID userId,
    @NotBlank @Size(max = 500) String content
) {
}
