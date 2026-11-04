package com.sprint.deokhugam.comment.adapter.in.web.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 등록 정보")
public record CommentCreateRequest(
    @Schema(description = "리뷰 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @NotNull(message = "리뷰 ID는 필수입니다.")
    UUID reviewId,

    @Schema(description = "작성자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @NotNull(message = "사용자 ID는 필수입니다.")
    UUID userId,

    @Schema(description = "댓글 내용 (최대 500자)", example = "좋은 리뷰네요!")
    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(max = 500, message = "댓글은 500자 이하여야 합니다.")
    String content
) {
}
