package com.sprint.deokhugam.dashboard.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.sprint.deokhugam.dashboard.domain.PeriodType;

public record PopularReviewDto(
    UUID id,
    UUID reviewId,
    UUID bookId,
    String bookTitle,
    String bookThumbnailUrl,
    UUID userId,
    String userNickname,
    String reviewContent,
    double reviewRating,
    PeriodType period,
    Instant createdAt,
    long rank,
    double score,
    long likeCount,
    long commentCount
) {
}
