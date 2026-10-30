package com.sprint.deokhugam.dashboard.adapter.in.scheduler;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobExecutionException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.dashboard.adapter.in.batch.DailyRankingJobConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {
    /**   private final PopularBookRankingUseCase popularBookRankingUseCase;
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
     } **/ // 스케줄러 코드 유기

    private final JobOperator jobOperator;
    private final Job dailyRankingJob;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void rankDailyRanking() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        JobParameters jobParameters = new JobParametersBuilder()
            .addLocalDate(DailyRankingJobConfig.BASE_DATE, today)
            .toJobParameters();

        try {
            JobExecution execution = jobOperator.start(dailyRankingJob, jobParameters);
            if (execution.getStatus() != BatchStatus.COMPLETED) {
                log.error("일일 랭킹 배치 실패: baseDate={}, status={}", today, execution.getStatus());
            }
        } catch (JobExecutionException ex) {
            log.error("일일 랭킹 배치를 시작하지 못함: baseDate={}", today, ex);
        }
    }

}
