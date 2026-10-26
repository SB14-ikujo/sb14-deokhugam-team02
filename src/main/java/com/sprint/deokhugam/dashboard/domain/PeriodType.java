package com.sprint.deokhugam.dashboard.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public enum PeriodType {
    DAILY(1, "일간"),
    WEEKLY(7, "주간"),
    MONTHLY(30, "월간"),
    ALL_TIME(null, "역대");

    // 아시아 서울 기준
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    Integer days;
    String label;

    public Instant startOf(LocalDate baseDate) {
        if (days == null) {
            return null;
        }
        // atStartOfDay : 0시로 만들기
        return baseDate.minusDays(days).atStartOfDay(ZONE).toInstant();
    }

    public static Instant endOf(LocalDate baseDate) {
        return baseDate.atStartOfDay(ZONE).toInstant();
    }
}
