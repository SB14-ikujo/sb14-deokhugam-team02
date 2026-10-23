package com.sprint.deokhugam.comment.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "댓글 목록 조회 조건")
public record CommentListRequest(
    @Schema(description = "리뷰 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @NotNull(message = "리뷰 ID는 필수입니다.")
    UUID reviewId,

    @Schema(description = "정렬 방향 (생성 시간 기준)", defaultValue = "DESC")
    Sort.Direction direction,

    @Schema(description = "커서 (이전 페이지 마지막 댓글 ID)")
    String cursor,

    @Schema(description = "보조 커서 (이전 페이지 마지막 댓글 생성 시간)")
    Instant after,

    @Min(value = 1, message = "limit은 1 이상이어야 합니다.")
    @Max(value = 100, message = "limit은 100 이하여야 합니다.")
    @Schema(description = "페이지 크기", defaultValue = "50", example = "50")
    Integer limit
) {
    public CommentListRequest {
        if (direction == null) {
            direction = Sort.Direction.DESC;
        }
        if (limit == null) {
            limit = 50;
        }
    }
}
