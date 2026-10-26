package com.sprint.deokhugam.dashboard.adapter.in.web.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import com.sprint.deokhugam.dashboard.domain.Direction;
import com.sprint.deokhugam.dashboard.domain.PeriodType;

public record PopularReviewRequest(
    @Schema(description = "랭킹 기간", defaultValue = "DAILY")
    PeriodType period,

    @Schema(description = "정렬 방향", defaultValue = "ASC")
    Direction direction,

    @Schema(description = "커서 페이지네이션 커서")
    String cursor,

    @Schema(description = "보조 커서(createdAt)")
    Instant after,

    @Min(1)
    @Max(100)
    @Schema(description = "페이지 크기", defaultValue = "50", example = "50")
    Integer limit
) {
    public PopularReviewRequest {
        if (period == null) {
            period = PeriodType.DAILY;
        }
        if (direction == null) {
            direction = Direction.ASC;
        }
        if (limit == null) {
            limit = 50;
        }
    }
}
