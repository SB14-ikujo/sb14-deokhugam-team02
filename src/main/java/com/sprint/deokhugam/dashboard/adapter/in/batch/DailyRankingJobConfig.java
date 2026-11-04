package com.sprint.deokhugam.dashboard.adapter.in.batch;

import java.time.LocalDate;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.dashboard.application.port.in.popularbook.PopularBookRankingUseCase;
import com.sprint.deokhugam.dashboard.application.port.in.popularreview.PopularReviewRankingUseCase;
import com.sprint.deokhugam.dashboard.application.port.in.poweruser.PowerUserRankingUseCase;
import com.sprint.deokhugam.notification.application.port.in.NotificationUseCase;

@Configuration
@RequiredArgsConstructor
public class DailyRankingJobConfig {
    public static final String JOB_NAME = "dailyRankingJob";
    public static final String BASE_DATE = "baseDate";
    private final MeterRegistry meterRegistry;   // 메트릭 저장소 (Actuator가 자동으로 만들어 줌)

    private final JobRepository jobRepository;  // 배치 기록 및 판단
    private final PlatformTransactionManager transactionManager; // step(각각 도서, 리뷰 같은거) 단위 트랜잭션
    private final PopularBookRankingUseCase popularBookRankingUseCase;
    private final PopularReviewRankingUseCase popularReviewRankingUseCase;
    private final PowerUserRankingUseCase powerUserRankingUseCase;
    private final NotificationUseCase notificationUseCase;

    @Bean
    public Job dailyRankingJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(popularBookStep())
            .next(popularReviewStep())
            .next(popularReviewNotificationStep())
            .next(powerUserStep())
            .build();
    }

    @Bean
    public Step popularBookStep() {
        return new StepBuilder("popularBookStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                int count = popularBookRankingUseCase.rankBooks(baseDate(contribution));
                recordCount(contribution, "popularBookStep", count);
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }

    @Bean
    public Step popularReviewStep() {
        return new StepBuilder("popularReviewStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                int count = popularReviewRankingUseCase.rankReviews(baseDate(contribution));
                recordCount(contribution, "popularReviewStep", count);
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }

    @Bean
    public Step powerUserStep() {
        return new StepBuilder("powerUserStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                int count = powerUserRankingUseCase.rankUsers(baseDate(contribution));
                recordCount(contribution, "powerUserStep", count);
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }

    @Bean
    public Step popularReviewNotificationStep() {
        return new StepBuilder("popularReviewNotificationStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                long count = notificationUseCase.createTop10ReviewNotifications(baseDate(contribution));
                recordCount(contribution, "popularReviewNotificationStep", count);
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }

    // Job을 실행할 때 넘긴 기준 날짜를 꺼낸다
    private LocalDate baseDate(StepContribution contribution) {
        return contribution.getStepExecution().getJobParameters().getLocalDate(BASE_DATE);
    }

    private void recordCount(StepContribution contribution, String stepName, long count) {
        contribution.incrementWriteCount(count);
        meterRegistry.counter("deokhugam.batch.ranked.count", "step", stepName).increment(count);
    }
}
