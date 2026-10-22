package com.sprint.deokhugam.dashboard.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public enum PeriodType {
    DAILY(1),
    WEEKLY(7),
    MONTHLY(30),
    ALL_TIME(null);

    // 아시아 서울 기준
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private final Integer days;

    PeriodType(Integer days) {
        this.days = days;
    }

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
