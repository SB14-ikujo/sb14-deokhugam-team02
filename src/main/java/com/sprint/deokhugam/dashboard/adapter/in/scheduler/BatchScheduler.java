package com.sprint.deokhugam.dashboard.adapter.in.scheduler;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.dashboard.application.port.in.popularbook.PopularBookRankingUseCase;
import com.sprint.deokhugam.dashboard.application.port.in.popularreview.PopularReviewRankingUseCase;
import com.sprint.deokhugam.dashboard.application.port.in.poweruser.PowerUserRankingUseCase;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {
    private final PopularBookRankingUseCase popularBookRankingUseCase;
    private final PopularReviewRankingUseCase popularReviewRankingUseCase;
    private final PowerUserRankingUseCase powerUserRankingUseCase;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void rankDailyRanking() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        runSafely("인기 도서", () -> popularBookRankingUseCase.rankBooks(today));
        runSafely("인기 리뷰", () -> popularReviewRankingUseCase.rankReviews(today));
        runSafely("파워 유저", () -> powerUserRankingUseCase.rankUsers(today));
    }

    private void runSafely(String name, Runnable task) {
        try {
            task.run();
        } catch (Exception ex) {
            log.error("{} 집계 실패", name, ex);
        }
    }
}
