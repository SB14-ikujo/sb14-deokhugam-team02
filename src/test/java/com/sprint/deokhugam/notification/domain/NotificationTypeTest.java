package com.sprint.deokhugam.notification.domain;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sprint.deokhugam.notification.domain.exception.NotificationException;

@ExtendWith(MockitoExtension.class)
class NotificationTypeTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Label"})
    @DisplayName("유효하지 않은 알림 타입이면 예외가 발생한다.")
    void from_invalidNotificationType(String type) {
        assertThrows(NotificationException.class, () -> NotificationType.from(type));
    }

    @Test
    @DisplayName("유효하지 않은 알림 타입이면 예외가 발생한다.")
    void from_invalidNotificationType() {
        assertThrows(NotificationException.class, () -> NotificationType.from("Label"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"comment", "LIKE"})
    @DisplayName("대소문자 구분없이 유효한 타입은 NotificationType 반환한다.")
    void from_validNotificationType(String type) {
        NotificationType notificationType = NotificationType.from(type);
        assertThat(notificationType).isInstanceOf(NotificationType.class);
    }

}
