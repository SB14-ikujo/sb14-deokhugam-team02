package com.sprint.deokhugam.notification.adapter.in.scheduler;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.notification.application.port.in.NotificationUseCase;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReadNotificationCleanupScheduler {
    private final NotificationUseCase notificationUseCase;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul") // 오전 12:00
    public void deleteReadNotifications() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        log.info("[읽은 알림 삭제 배치] 시작 - 기준일: {}", today);
        long deleted = notificationUseCase.deleteReadNotifications(today);

        log.info("[읽은 알림 삭제 배치] 종료 - 완료: {}건", deleted);
    }
}
