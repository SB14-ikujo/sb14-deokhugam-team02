package com.sprint.deokhugam.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import lombok.extern.slf4j.Slf4j;

/**
 * 스케줄러(@Scheduled) 실행 설정.
 * 기본 스케줄러는 스레드가 1개라 배치가 하나씩만 돌기 때문에,
 * 전용 스레드 풀을 만들어 여러 배치가 동시에 실행될 수 있게 한다.
 * 배치에서 처리되지 않은 예외도 여기서 한 곳으로 모아 로그를 남긴다.
 */
@Slf4j
@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {

    @Override // 모든 @Scheduled 작업이 아래에서 만든 스케줄러로 실행되도록 등록한다.
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler());
    }

    @Bean // 배치 전용 스케줄러 : 스레드 3개 / 스래드 이름 - deokhugam-batch / 에러 핸들러: 배치 실행중 잡히 않는 예외 로그 남김
    public TaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(3);
        scheduler.setThreadNamePrefix("deokhugam-batch-");

        // 예외 핸들러 등록
        scheduler.setErrorHandler(error -> {
            log.error("[경고] 스케줄러 배치 작업 중 알 수 없는 오류가 발생했습니다.", error);
        });

        return scheduler;
    }
}
