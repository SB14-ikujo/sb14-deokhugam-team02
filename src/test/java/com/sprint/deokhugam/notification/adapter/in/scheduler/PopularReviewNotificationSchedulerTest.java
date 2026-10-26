package com.sprint.deokhugam.notification.adapter.in.scheduler;

import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sprint.deokhugam.notification.application.port.in.NotificationUseCase;

@ExtendWith(MockitoExtension.class)
class PopularReviewNotificationSchedulerTest {
    @Mock
    private NotificationUseCase notificationUseCase;

    @InjectMocks
    private PopularReviewNotificationScheduler scheduler;

    @Test
    @DisplayName("인기 리뷰 TOP 10 알림 생성 스케줄러가 실행된다")
    void createTop10ReviewNotifications_success() {
        LocalDate baseDate = LocalDate.now(ZoneId.of("Asia/Seoul"));
        // given
        given(notificationUseCase.createTop10ReviewNotifications(baseDate))
            .willReturn(1L);
        ;

        // when
        scheduler.createTop10ReviewNotifications();

        // then
        verify(notificationUseCase).createTop10ReviewNotifications(baseDate);

    }

}
