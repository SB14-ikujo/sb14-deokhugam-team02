package com.sprint.deokhugam.notification.adapter.in.scheduler;

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

import com.sprint.deokhugam.notification.adapter.in.batch.ReadNotificationCleanupJobConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReadNotificationCleanupScheduler {

    private final JobOperator jobOperator;
    private final Job readNotificationCleanupJob;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul") // 오전 12:00
    public void deleteReadNotifications() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        JobParameters jobParameters = new JobParametersBuilder()
            .addLocalDate(ReadNotificationCleanupJobConfig.BASE_DATE, today)
            .toJobParameters();

        try {
            JobExecution execution = jobOperator.start(readNotificationCleanupJob, jobParameters);
            if (execution.getStatus() != BatchStatus.COMPLETED) {
                log.error("읽은 알림 삭제 배치 실패: baseDate={}, status={}", today, execution.getStatus());
            }
        } catch (JobExecutionException ex) {
            log.error("읽은 알림 삭제 배치를 시작하지 못함: baseDate={}", today, ex);
        }
    }
}
