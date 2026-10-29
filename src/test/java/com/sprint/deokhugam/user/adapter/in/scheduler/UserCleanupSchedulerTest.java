package com.sprint.deokhugam.user.adapter.in.scheduler;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;

import com.sprint.deokhugam.user.application.port.in.UserUseCase;

@ExtendWith(MockitoExtension.class)
public class UserCleanupSchedulerTest {
    @Mock
    private UserUseCase userUseCase;

    @InjectMocks
    private UserCleanupScheduler scheduler;

    @Test
    @DisplayName("사용자 물리 삭제 스케줄러가 실행되면 현재 시각으로 삭제를 요청한다.")
    void deleteExpiredUsers_success() {
        // given
        given(userUseCase.deleteExpiredUsers(any(Instant.class))).willReturn(2);
        Instant before = Instant.now();

        // when
        scheduler.deleteExpiredUsers();

        // then
        Instant after = Instant.now();
        ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
        then(userUseCase).should().deleteExpiredUsers(captor.capture());
        assertThat(captor.getValue()).isBetween(before, after);
    }

    @Test
    @DisplayName("사용자 물리 삭제 스케줄러는 매일 한국 시간 23시 30분에 실행된다.")
    void runsAt2330Kst() throws NoSuchMethodException {
        // given
        Scheduled scheduled = UserCleanupScheduler.class
            .getMethod("deleteExpiredUsers")
            .getAnnotation(Scheduled.class);
        ZoneId zone = ZoneId.of(scheduled.zone());
        ZonedDateTime beforeRun = ZonedDateTime.of(2026, 10, 29, 23, 29, 59, 0, zone);

        // when
        ZonedDateTime next = CronExpression.parse(scheduled.cron()).next(beforeRun);

        // then
        assertThat(scheduled.zone()).isEqualTo("Asia/Seoul");
        assertThat(next).isEqualTo(ZonedDateTime.of(2026, 10, 29, 23, 30, 0, 0, zone));
    }
}
