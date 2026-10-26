package com.sprint.deokhugam.dashboard.adapter.in.scheduler;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.dashboard.application.port.in.popularbook.PopularBookRankingUseCase;
import com.sprint.deokhugam.dashboard.application.port.in.popularreview.PopularReviewRankingUseCase;

@Component
@RequiredArgsConstructor
public class BatchScheduler {
    private final PopularBookRankingUseCase popularBookRankingUseCase;
    private final PopularReviewRankingUseCase popularReviewRankingUseCase;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void rankDailyRanking() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        popularBookRankingUseCase.rankBooks(today);
        popularReviewRankingUseCase.rankReviews(today);
    }
}
