package com.sprint.deokhugam.notification.adapter.in.scheduler;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.ZoneId;

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

import com.sprint.deokhugam.notification.adapter.in.batch.ReadNotificationCleanupJobConfig;

@ExtendWith(MockitoExtension.class)
class ReadNotificationCleanupSchedulerTest {

    @Mock
    private JobOperator jobOperator;

    @Mock
    private Job readNotificationCleanupJob;

    @InjectMocks
    private ReadNotificationCleanupScheduler scheduler;

    @Test
    @DisplayName("읽은 알림 삭제 스케줄러가 실행되면 오늘 날짜를 기준 날짜로 넘겨 Job을 시작한다")
    void deleteReadNotifications_success() throws Exception {
        // given
        JobExecution execution = executionWithStatus(BatchStatus.COMPLETED);
        given(jobOperator.start(eq(readNotificationCleanupJob), any(JobParameters.class)))
            .willReturn(execution);

        // when
        scheduler.deleteReadNotifications();

        // then
        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobOperator).start(eq(readNotificationCleanupJob), captor.capture());
        assertThat(captor.getValue().getLocalDate(ReadNotificationCleanupJobConfig.BASE_DATE))
            .isEqualTo(LocalDate.now(ZoneId.of("Asia/Seoul")));
    }

    @Test
    @DisplayName("Job이 실패 상태로 끝나도 예외가 스케줄러 밖으로 나가지 않는다")
    void deleteReadNotifications_failedJob() throws Exception {
        // given
        JobExecution execution = executionWithStatus(BatchStatus.FAILED);
        given(jobOperator.start(eq(readNotificationCleanupJob), any(JobParameters.class)))
            .willReturn(execution);

        // when & then
        assertThatCode(() -> scheduler.deleteReadNotifications()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("같은 날짜의 Job이 이미 완료돼 시작하지 못해도 예외가 스케줄러 밖으로 나가지 않는다")
    void deleteReadNotifications_startFailure() throws Exception {
        // given
        given(jobOperator.start(eq(readNotificationCleanupJob), any(JobParameters.class)))
            .willThrow(new JobInstanceAlreadyCompleteException("이미 완료된 Job"));

        // when & then
        assertThatCode(() -> scheduler.deleteReadNotifications()).doesNotThrowAnyException();
    }

    private JobExecution executionWithStatus(BatchStatus status) {
        JobExecution execution = mock(JobExecution.class);
        given(execution.getStatus()).willReturn(status);
        return execution;
    }
}
