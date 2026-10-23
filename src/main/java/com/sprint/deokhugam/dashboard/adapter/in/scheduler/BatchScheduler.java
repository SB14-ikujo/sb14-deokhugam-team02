package com.sprint.deokhugam.dashboard.adapter.in.scheduler;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.dashboard.application.port.in.popularbook.PopularBookRankingUseCase;

@Component
@RequiredArgsConstructor
public class BatchScheduler {
    private final PopularBookRankingUseCase popularBookRankingUseCase;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void rankDailyRanking() {
        popularBookRankingUseCase.rankBooks(LocalDate.now(ZoneId.of("Asia/Seoul")));
    }
}
