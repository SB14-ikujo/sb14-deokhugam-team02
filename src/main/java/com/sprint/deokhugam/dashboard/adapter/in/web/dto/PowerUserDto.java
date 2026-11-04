package com.sprint.deokhugam.dashboard.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.sprint.deokhugam.dashboard.domain.PeriodType;

public record PowerUserDto(
    UUID userId,
    String nickname,
    PeriodType period,
    Instant createdAt,
    long rank,
    double score,
    double reviewScoreSum,
    long likeCount,
    long commentCount
) {
}
