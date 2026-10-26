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
public class PopularReviewNotificationScheduler {
    private final NotificationUseCase notificationUseCase;

    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")  // 오전 1:00
    public void createTop10ReviewNotifications() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        log.info("[인기 리뷰 알림 생성 배치] 시작 - 기준일: {}", today);
        long created = notificationUseCase.createTop10ReviewNotifications(today);
        log.info("[인기 리뷰 알림 생성 배치] 종료 - 완료: {}건", created);
    }
}
