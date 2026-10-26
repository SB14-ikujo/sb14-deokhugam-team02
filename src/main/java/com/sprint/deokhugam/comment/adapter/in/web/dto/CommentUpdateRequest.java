package com.sprint.deokhugam.comment.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 수정 정보")
public record CommentUpdateRequest(
    @Schema(description = "수정할 댓글 내용 (최대 500자)", example = "수정된 댓글입니다.")
    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(max = 500, message = "댓글은 500자 이하여야 합니다.")
    String content
) {
}
