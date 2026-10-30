package com.sprint.deokhugam.dashboard.adapter.in.batch;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
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

import io.micrometer.core.instrument.MeterRegistry;

import com.sprint.deokhugam.dashboard.application.port.in.popularbook.PopularBookRankingUseCase;
import com.sprint.deokhugam.dashboard.application.port.in.popularreview.PopularReviewRankingUseCase;
import com.sprint.deokhugam.dashboard.application.port.in.poweruser.PowerUserRankingUseCase;
import com.sprint.deokhugam.notification.application.port.in.NotificationUseCase;

@SpringBootTest
@SpringBatchTest
class DailyRankingJobConfigTest {

    private static final LocalDate BASE_DATE = LocalDate.of(2026, 10, 19);
    private static final String COUNT_METRIC = "deokhugam.batch.ranked.count";

    @Autowired
    private JobOperatorTestUtils jobOperatorTestUtils;

    @Autowired
    private Job dailyRankingJob;

    @Autowired
    private MeterRegistry meterRegistry;

    @MockitoBean
    private PopularBookRankingUseCase popularBookRankingUseCase;

    @MockitoBean
    private PopularReviewRankingUseCase popularReviewRankingUseCase;

    @MockitoBean
    private PowerUserRankingUseCase powerUserRankingUseCase;

    @MockitoBean
    private NotificationUseCase notificationUseCase;

    // Job 빈이 여러 개일 때도 이 Job을 실행하도록 직접 지정한다
    @BeforeEach
    void setUp() {
        jobOperatorTestUtils.setJob(dailyRankingJob);
    }

    @Test
    @DisplayName("Job을 실행하면 기준 날짜로 인기 도서, 인기 리뷰, 인기 리뷰 알림, 파워 유저 순서로 처리하고 완료된다")
    void dailyRankingJobRunsStepsInOrder() throws Exception {
        JobExecution execution = jobOperatorTestUtils.startJob(jobParameters());

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getStepExecutions())
            .extracting(StepExecution::getStepName)
            .containsExactly("popularBookStep", "popularReviewStep", "popularReviewNotificationStep", "powerUserStep");

        InOrder inOrder = inOrder(
            popularBookRankingUseCase, popularReviewRankingUseCase, notificationUseCase, powerUserRankingUseCase);
        inOrder.verify(popularBookRankingUseCase).rankBooks(BASE_DATE);
        inOrder.verify(popularReviewRankingUseCase).rankReviews(BASE_DATE);
        inOrder.verify(notificationUseCase).createTop10ReviewNotifications(BASE_DATE);
        inOrder.verify(powerUserRankingUseCase).rankUsers(BASE_DATE);
    }

    @Test
    @DisplayName("Step마다 처리한 건수를 배치 기록의 write count와 메트릭에 남긴다")
    void dailyRankingJobRecordsCount() throws Exception {
        when(popularBookRankingUseCase.rankBooks(any())).thenReturn(3);
        when(popularReviewRankingUseCase.rankReviews(any())).thenReturn(5);
        when(notificationUseCase.createTop10ReviewNotifications(any())).thenReturn(10L);
        when(powerUserRankingUseCase.rankUsers(any())).thenReturn(7);
        double bookCountBefore = countMetric("popularBookStep");
        double reviewCountBefore = countMetric("popularReviewStep");
        double notificationCountBefore = countMetric("popularReviewNotificationStep");
        double userCountBefore = countMetric("powerUserStep");

        JobExecution execution = jobOperatorTestUtils.startJob(jobParameters());

        Map<String, Long> writeCounts = execution.getStepExecutions().stream()
            .collect(Collectors.toMap(StepExecution::getStepName, StepExecution::getWriteCount));
        assertThat(writeCounts)
            .containsEntry("popularBookStep", 3L)
            .containsEntry("popularReviewStep", 5L)
            .containsEntry("popularReviewNotificationStep", 10L)
            .containsEntry("powerUserStep", 7L);
        assertThat(countMetric("popularBookStep") - bookCountBefore).isEqualTo(3.0);
        assertThat(countMetric("popularReviewStep") - reviewCountBefore).isEqualTo(5.0);
        assertThat(countMetric("popularReviewNotificationStep") - notificationCountBefore).isEqualTo(10.0);
        assertThat(countMetric("powerUserStep") - userCountBefore).isEqualTo(7.0);
    }

    @Test
    @DisplayName("인기 리뷰 집계가 실패하면 Job은 실패하고 인기 리뷰 알림과 파워 유저 Step은 실행되지 않는다")
    void dailyRankingJobStopsWhenReviewStepFails() throws Exception {
        when(popularReviewRankingUseCase.rankReviews(any())).thenThrow(new IllegalStateException("리뷰 집계 실패"));

        JobExecution execution = jobOperatorTestUtils.startJob(jobParameters());

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(execution.getStepExecutions())
            .extracting(StepExecution::getStepName, StepExecution::getStatus)
            .containsExactly(
                tuple("popularBookStep", BatchStatus.COMPLETED),
                tuple("popularReviewStep", BatchStatus.FAILED));
        verify(notificationUseCase, never()).createTop10ReviewNotifications(any());
        verify(powerUserRankingUseCase, never()).rankUsers(any());
    }

    // 같은 기준 날짜로 여러 번 실행할 수 있게 고유한 파라미터를 하나 더 붙인다
    private JobParameters jobParameters() {
        return jobOperatorTestUtils.getUniqueJobParametersBuilder()
            .addLocalDate(DailyRankingJobConfig.BASE_DATE, BASE_DATE)
            .toJobParameters();
    }

    private double countMetric(String stepName) {
        return meterRegistry.counter(COUNT_METRIC, "step", stepName).count();
    }
}
