package com.sprint.deokhugam.review.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "리뷰 목록 조회 조건")
public record ReviewListRequest(
    @Schema(description = "특정 유저 필터 (선택)", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID userId,

    @Schema(description = "특정 도서 필터 (선택)", example = "987f6543-e89b-12d3-a456-426614174000")
    UUID bookId,

    @Schema(description = "검색 키워드 (작성자 닉네임, 내용, 도서 제목)", example = "해리포터")
    String keyword,

    @Schema(description = "정렬 기준 (rating: 평점순, createdAt: 시간순)", defaultValue = "createdAt", example = "rating")
    String orderBy,

    @Schema(description = "정렬 방향 (ASC: 오름차순, DESC: 내림차순)", defaultValue = "DESC")
    Sort.Direction direction,

    @Schema(description = "메인 커서 (평점순 정렬일 때는 평점 문자열, 시간순 정렬일 때는 사용 안함)", example = "4.0")
    String cursor,

    @Schema(description = "보조 커서 (이전 페이지 마지막 리뷰의 생성 시간 - 항상 필수)", example = "2026-10-29T12:00:00Z")
    Instant after,

    @Min(value = 1, message = "limit은 1 이상이어야 합니다.")
    @Max(value = 100, message = "limit은 100 이하여야 합니다.")
    @Schema(description = "페이지 크기", defaultValue = "50", example = "50")
    Integer limit
) {
    public ReviewListRequest {
        if (orderBy == null || orderBy.isBlank()) {
            orderBy = "createdAt"; // 기본값은 최신순 정렬
        }
        if (direction == null) {
            direction = Sort.Direction.DESC; // 기본값은 내림차순(최신순, 높은평점순)
        }
        if (limit == null) {
            limit = 50; // 기본 페이지 크기는 50개
        }
    }
}
