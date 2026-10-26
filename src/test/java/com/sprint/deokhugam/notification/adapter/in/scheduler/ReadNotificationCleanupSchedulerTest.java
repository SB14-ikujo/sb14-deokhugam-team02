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
class ReadNotificationCleanupSchedulerTest {

    @Mock
    private NotificationUseCase notificationUseCase;

    @InjectMocks
    private ReadNotificationCleanupScheduler scheduler;

    @Test
    @DisplayName("읽은 알림 삭제 스케줄러가 실행되면 오늘 날짜로 삭제를 요청한다")
    void deleteReadNotifications_success() {
        // given
        given(notificationUseCase.deleteReadNotifications(any(LocalDate.class)))
            .willReturn(3L);

        // when
        scheduler.deleteReadNotifications();

        // then
        verify(notificationUseCase).deleteReadNotifications(LocalDate.now(ZoneId.of("Asia/Seoul")));
    }
}
