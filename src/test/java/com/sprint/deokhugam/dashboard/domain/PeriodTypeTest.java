package com.sprint.deokhugam.dashboard.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PeriodTypeTest {
    private static final LocalDate BASE_DATE = LocalDate.of(2026, 10, 19);
    private static final LocalDate MONTHLY_DATE = LocalDate.of(2026, 10, 1);
    private static final LocalDate WEEKLY_DATE = LocalDate.of(2026, 10, 5);
    private static final LocalDate DAILY_DATE = LocalDate.of(2026, 10, 22);

    @ParameterizedTest(name = "{0} 시작 시각 = {1}")
    @CsvSource({
        "DAILY,   2026-10-17T15:00:00Z",
        "WEEKLY,  2026-10-11T15:00:00Z",
        "MONTHLY, 2026-09-18T15:00:00Z"
    })
    @DisplayName("기간별 시작 시각은 기준일에서 일수만큼 뺀 날의 한국 시간 0시다")
    void startOf(PeriodType period, Instant expected) {
        assertThat(period.startOf(BASE_DATE)).isEqualTo(expected);
    }

    @Test
    @DisplayName("역대 기간은 시작 시각이 없다")
    void startOfAllTimeIsNull() {
        assertThat(PeriodType.ALL_TIME.startOf(BASE_DATE)).isNull();
    }

    @Test
    @DisplayName("끝 시각은 기준일의 한국 시간 0시다")
    void endOf() {
        assertThat(PeriodType.endOf(BASE_DATE)).isEqualTo(Instant.parse("2026-10-18T15:00:00Z"));
    }

    @Test
    @DisplayName("월이 바뀌는 날에도 전날을 정확히 계산한다")
    void startOfAcrossMonth() {
        LocalDate firstOfMarch = LocalDate.of(2026, 3, 1);

        assertThat(PeriodType.DAILY.startOf(firstOfMarch)).isEqualTo(Instant.parse("2026-02-27T15:00:00Z"));
    }

    @Test
    @DisplayName("일간은 매일 알림을 생성한다")
    void isNotifyDay_daily() {
        PeriodType periodType = PeriodType.DAILY;

        assertThat(periodType.isNotifyDay(MONTHLY_DATE)).isTrue();
    }

    @Test
    @DisplayName("주간은 월요일에만 알림을 생성한다")
    void isNotifyDay_weekly() {
        PeriodType periodType = PeriodType.WEEKLY;
        assertThat(periodType.isNotifyDay(WEEKLY_DATE)).isTrue();
        assertThat(periodType.isNotifyDay(WEEKLY_DATE.plusDays(1))).isFalse();
    }

    @Test
    @DisplayName("월간은 매월 1일에만 알림을 생성한다")
    void isNotifyDay_monthly() {
        PeriodType periodType = PeriodType.MONTHLY;
        assertThat(periodType.isNotifyDay(MONTHLY_DATE)).isTrue();
        assertThat(periodType.isNotifyDay(MONTHLY_DATE.plusDays(2))).isFalse();

    }
}
