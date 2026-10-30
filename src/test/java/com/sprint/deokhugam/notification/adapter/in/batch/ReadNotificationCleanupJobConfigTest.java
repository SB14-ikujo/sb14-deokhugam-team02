package com.sprint.deokhugam.notification.adapter.in.batch;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.sprint.deokhugam.notification.application.port.in.NotificationUseCase;

@SpringBootTest
@SpringBatchTest
class ReadNotificationCleanupJobConfigTest {

    private static final LocalDate BASE_DATE = LocalDate.of(2026, 10, 19);

    @Autowired
    private JobOperatorTestUtils jobOperatorTestUtils;

    @Autowired
    private Job readNotificationCleanupJob;

    @MockitoBean
    private NotificationUseCase notificationUseCase;

    // Job 빈이 여러 개라서 이 Job을 실행하도록 직접 지정한다
    @BeforeEach
    void setUp() {
        jobOperatorTestUtils.setJob(readNotificationCleanupJob);
    }

    @Test
    @DisplayName("Job을 실행하면 기준 날짜로 읽은 알림을 삭제하고 삭제 건수를 write count에 남긴다")
    void readNotificationCleanupJob_success() throws Exception {
        // given
        given(notificationUseCase.deleteReadNotifications(BASE_DATE)).willReturn(4L);

        // when
        JobExecution execution = jobOperatorTestUtils.startJob(jobParameters());

        // then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getStepExecutions())
            .extracting(StepExecution::getStepName, StepExecution::getWriteCount)
            .containsExactly(tuple("readNotificationCleanupStep", 4L));
        verify(notificationUseCase).deleteReadNotifications(BASE_DATE);
    }

    @Test
    @DisplayName("삭제 중 예외가 나면 Job은 실패 상태로 끝난다")
    void readNotificationCleanupJob_failure() throws Exception {
        // given
        given(notificationUseCase.deleteReadNotifications(any())).willThrow(new IllegalStateException("삭제 실패"));

        // when
        JobExecution execution = jobOperatorTestUtils.startJob(jobParameters());

        // then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.FAILED);
    }

    // 같은 기준 날짜로 여러 번 실행할 수 있게 고유한 파라미터를 하나 더 붙인다
    private JobParameters jobParameters() {
        return jobOperatorTestUtils.getUniqueJobParametersBuilder()
            .addLocalDate(ReadNotificationCleanupJobConfig.BASE_DATE, BASE_DATE)
            .toJobParameters();
    }
}
