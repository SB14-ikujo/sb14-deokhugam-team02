package com.sprint.deokhugam.dashboard.domain;

import java.time.DayOfWeek;
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

    /**
     * 주어진 날짜가 이 기간의 인기 리뷰 알림을 보내는 날인지 확인한다.
     * DAILY: 매일 / WEEKLY: 매주 월요일 / MONTHLY: 매월 1일 / ALL_TIME: 보내지 않음
     */
    public boolean isNotifyDay(LocalDate date) {
        return switch (this) {
            case DAILY -> true;
            case WEEKLY -> date.getDayOfWeek() == DayOfWeek.MONDAY;
            case MONTHLY -> date.getDayOfMonth() == 1;
            case ALL_TIME -> false;
        };
    }
}
