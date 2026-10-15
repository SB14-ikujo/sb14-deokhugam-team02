package com.sprint.deokhugam.dashboard.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.sprint.deokhugam.dashboard.domain.PeriodType;

public record PopularBookDto(
    UUID id,
    UUID bookId,
    String title,
    String author,
    String thumbnailUrl,
    PeriodType period,
    long rank,
    double score,
    long reviewCount,
    double rating,
    Instant createdAt
) {
}
