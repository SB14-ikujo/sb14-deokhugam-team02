package com.sprint.deokhugam.dashboard.adapter.in.scheduler;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;

import com.sprint.deokhugam.dashboard.adapter.in.batch.DailyRankingJobConfig;

@ExtendWith(MockitoExtension.class)
class BatchSchedulerTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    @Mock
    private JobOperator jobOperator;

    @Mock
    private Job dailyRankingJob;

    @InjectMocks
    private BatchScheduler batchScheduler;

    @Test
    @DisplayName("일일 랭킹을 실행하면 한국 시간 기준 오늘 날짜를 기준 날짜로 넘겨 Job을 시작한다")
    void rankDailyRankingStartsJobWithToday() throws Exception {
        JobExecution execution = executionWithStatus(BatchStatus.COMPLETED);
        when(jobOperator.start(eq(dailyRankingJob), any(JobParameters.class))).thenReturn(execution);

        batchScheduler.rankDailyRanking();

        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobOperator).start(eq(dailyRankingJob), captor.capture());
        assertThat(captor.getValue().getLocalDate(DailyRankingJobConfig.BASE_DATE)).isEqualTo(LocalDate.now(ZONE));
    }

    @Test
    @DisplayName("Job이 실패 상태로 끝나도 예외가 스케줄러 밖으로 나가지 않는다")
    void rankDailyRankingSwallowsFailedJob() throws Exception {
        JobExecution execution = executionWithStatus(BatchStatus.FAILED);
        when(jobOperator.start(eq(dailyRankingJob), any(JobParameters.class))).thenReturn(execution);

        assertThatCode(() -> batchScheduler.rankDailyRanking()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("같은 날짜의 Job이 이미 완료돼 시작하지 못해도 예외가 스케줄러 밖으로 나가지 않는다")
    void rankDailyRankingSwallowsStartFailure() throws Exception {
        when(jobOperator.start(eq(dailyRankingJob), any(JobParameters.class)))
            .thenThrow(new JobInstanceAlreadyCompleteException("이미 완료된 Job"));

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

    private JobExecution executionWithStatus(BatchStatus status) {
        JobExecution execution = mock(JobExecution.class);
        when(execution.getStatus()).thenReturn(status);
        return execution;
    }
}
