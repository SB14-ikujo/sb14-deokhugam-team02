package com.sprint.deokhugam.book.adapter.in.web.dto;

import java.time.Instant;

import org.springframework.data.domain.Sort;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(description = "도서 목록 조회 조건")
public record BookSearchRequest(
    @Schema(description = "도서 제목 | 저자 | ISBN", example = "자바")
    String keyword,

    @Schema(
        description = "정렬 기준 (title | publishedDate | rating | reviewCount)",
        defaultValue = "title", example = "title"
    )
    String orderBy,

    @Schema(description = "정렬 방향", allowableValues = {"ASC", "DESC"}, defaultValue = "DESC", example = "DESC")
    Sort.Direction direction,

    @Schema(description = "커서 페이지네이션 커서")
    String cursor,

    @Schema(description = "보조 커서(createdAt)")
    Instant after,

    @Min(value = 1, message = "limit은 1 이상이어야 합니다.")
    @Schema(description = "페이지 크기", defaultValue = "50", example = "50")
    Integer limit
) {
    public BookSearchRequest {
        if (orderBy == null) {
            orderBy = "title";
        }

        if (direction == null) {
            direction = Sort.Direction.DESC;
        }

        if (limit == null) {
            limit = 50;
        }
    }
}
