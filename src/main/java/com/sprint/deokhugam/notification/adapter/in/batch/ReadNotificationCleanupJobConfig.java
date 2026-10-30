package com.sprint.deokhugam.notification.adapter.in.batch;

import java.time.LocalDate;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.notification.application.port.in.NotificationUseCase;

@Configuration
@RequiredArgsConstructor
public class ReadNotificationCleanupJobConfig {
    public static final String JOB_NAME = "readNotificationCleanupJob";
    public static final String BASE_DATE = "baseDate";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final NotificationUseCase notificationUseCase;

    @Bean
    public Job readNotificationCleanupJob(JobRepository jobRepository) {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(readNotificationCleanupStep())
            .build();
    }

    @Bean
    public Step readNotificationCleanupStep() {
        return new StepBuilder("readNotificationCleanupStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                LocalDate baseDate =
                    contribution.getStepExecution().getJobParameters().getLocalDate(BASE_DATE);
                long deleted = notificationUseCase.deleteReadNotifications(baseDate);
                contribution.incrementWriteCount(deleted);
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }
}
