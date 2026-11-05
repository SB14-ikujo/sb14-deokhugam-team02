package com.sprint.deokhugam.logarchive.adapter.in.scheduler;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.logarchive.application.in.LogArchiveUseCase;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "deokhugam.storage.type", havingValue = "s3")
public class LogArchiveScheduler {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final LogArchiveUseCase logArchiveUseCase;

    @Scheduled(cron = "0 0 1 * * MON", zone = "Asia/Seoul")  // 매주 월요일 새벽 1시
    public void archive() {
        LocalDate today = LocalDate.now(SEOUL);
        log.info("[로그 아카이브 배치] 시작 - 기준일: {}", today);
        int uploaded = logArchiveUseCase.archive(LocalDate.now(SEOUL));
        log.info("[로그 아카이브 배치] 종료 - 업로드: {}건", uploaded);

    }
}
