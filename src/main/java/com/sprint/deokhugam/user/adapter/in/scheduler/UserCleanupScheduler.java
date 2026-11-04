package com.sprint.deokhugam.user.adapter.in.scheduler;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.user.application.port.in.UserUseCase;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final UserUseCase userUseCase;

    @Scheduled(cron = "0 30 23 * * *", zone = "Asia/Seoul") // 오후 11:30
    public void deleteExpiredUsers() {
        Instant now = Instant.now();
        log.info("[사용자 물리 삭제 배치] 시작 - 기준 시각: {}", now);
        int deleted = userUseCase.deleteExpiredUsers(now);
        log.info("[사용자 물리 삭제 배치] 종료 - 완료: {}건", deleted);
    }
}
