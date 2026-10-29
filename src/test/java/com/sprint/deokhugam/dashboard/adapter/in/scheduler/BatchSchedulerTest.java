package com.sprint.deokhugam.dashboard.adapter.in.scheduler;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;

import com.sprint.deokhugam.dashboard.application.port.in.popularbook.PopularBookRankingUseCase;
import com.sprint.deokhugam.dashboard.application.port.in.popularreview.PopularReviewRankingUseCase;
import com.sprint.deokhugam.dashboard.application.port.in.poweruser.PowerUserRankingUseCase;

@ExtendWith(MockitoExtension.class)
class BatchSchedulerTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    @Mock
    private PopularBookRankingUseCase popularBookRankingUseCase;

    @Mock
    private PopularReviewRankingUseCase popularReviewRankingUseCase;

    @Mock
    private PowerUserRankingUseCase powerUserRankingUseCase;

    @InjectMocks
    private BatchScheduler batchScheduler;

    @Test
    @DisplayName("일일 랭킹을 실행하면 한국 시간 기준 오늘 날짜로 인기 도서, 인기 리뷰, 파워 유저 순서로 집계한다")
    void rankDailyRankingRanksWithToday() {
        batchScheduler.rankDailyRanking();

        LocalDate today = LocalDate.now(ZONE);
        InOrder inOrder = inOrder(popularBookRankingUseCase, popularReviewRankingUseCase, powerUserRankingUseCase);
        inOrder.verify(popularBookRankingUseCase).rankBooks(today);
        inOrder.verify(popularReviewRankingUseCase).rankReviews(today);
        inOrder.verify(powerUserRankingUseCase).rankUsers(today);
    }

    @Test
    @DisplayName("인기 도서 집계가 실패해도 인기 리뷰 집계는 실행된다")
    void rankDailyRankingRanksReviewsEvenIfBooksFail() {
        doThrow(new IllegalStateException("도서 집계 실패")).when(popularBookRankingUseCase).rankBooks(any());

        assertThatCode(() -> batchScheduler.rankDailyRanking()).doesNotThrowAnyException();

        verify(popularReviewRankingUseCase).rankReviews(LocalDate.now(ZONE));
    }

    @Test
    @DisplayName("인기 리뷰 집계가 실패해도 예외가 스케줄러 밖으로 나가지 않는다")
    void rankDailyRankingSwallowsReviewFailure() {
        doThrow(new IllegalStateException("리뷰 집계 실패")).when(popularReviewRankingUseCase).rankReviews(any());

        assertThatCode(() -> batchScheduler.rankDailyRanking()).doesNotThrowAnyException();

        verify(popularBookRankingUseCase).rankBooks(LocalDate.now(ZONE));
        verify(powerUserRankingUseCase).rankUsers(LocalDate.now(ZONE));
    }

    @Test
    @DisplayName("파워 유저 집계가 실패해도 예외가 스케줄러 밖으로 나가지 않는다")
    void rankDailyRankingSwallowsPowerUserFailure() {
        doThrow(new IllegalStateException("파워 유저 집계 실패")).when(powerUserRankingUseCase).rankUsers(any());

        assertThatCode(() -> batchScheduler.rankDailyRanking()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("일일 랭킹은 매일 한국 시간 0시에 실행된다")
    void rankDailyRankingRunsAtMidnightInKorea() throws NoSuchMethodException {
        Scheduled scheduled = BatchScheduler.class.getMethod("rankDailyRanking").getAnnotation(Scheduled.class);
        CronExpression cron = CronExpression.parse(scheduled.cron());
        ZonedDateTime afternoon = ZonedDateTime.of(2026, 10, 23, 15, 30, 0, 0, ZONE);

        ZonedDateTime firstRun = cron.next(afternoon);
        ZonedDateTime secondRun = cron.next(firstRun);

        assertThat(scheduled.zone()).isEqualTo("Asia/Seoul");
        assertThat(firstRun).isEqualTo(ZonedDateTime.of(2026, 10, 24, 0, 0, 0, 0, ZONE));
        assertThat(secondRun).isEqualTo(ZonedDateTime.of(2026, 10, 25, 0, 0, 0, 0, ZONE));
    }
}
