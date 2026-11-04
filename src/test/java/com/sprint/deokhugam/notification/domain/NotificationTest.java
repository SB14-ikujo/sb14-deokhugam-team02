package com.sprint.deokhugam.notification.domain;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sprint.deokhugam.notification.domain.exception.NotificationException;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.user.domain.User;

@ExtendWith(MockitoExtension.class)
class NotificationTest {

    @Mock
    private Review review;

    @Mock
    private User user;

    @Test
    @DisplayName("알람을 생성한다.")
    void createNotification() {

        given(review.getUser()).willReturn(user);
        String message = "[누구누구]님이 리뷰를 좋아요합니다.";

        // when
        Notification notification = Notification.create(review, message);

        // then
        assertThat(notification.getUser()).isEqualTo(user);
        assertThat(notification.getReview()).isEqualTo(review);
        assertThat(notification.getMessage()).isEqualTo(message);
        assertThat(notification.isConfirmed()).isFalse();
    }

    @Test
    @DisplayName("리뷰가 null이면 알림 생성은 실패한다.")
    void createNotification_nullReview() {
        assertThatThrownBy(() -> Notification.create(null, "[누구누구님이] 리뷰를 좋아합니다."))
            .isInstanceOf(NotificationException.class);
    }

    @ParameterizedTest                  // 아래 소스들이 제공하는 값마다 테스트를 한 번씩 반복 실행 (@Test 대체)
    @NullAndEmptySource                 // null, "" 제공
    @ValueSource(strings = {" ", "\t"}) // 공백, 탭 제공 (isBlank 케이스)
    @DisplayName("메시지가 null이거나 비어있으면 알림 생성은 실패한다.")
    void createNotification_withNullOrBlankMessage(String message) {
        assertThatThrownBy(() -> Notification.create(review, message))
            .isInstanceOf(NotificationException.class);
    }

    @Test
    @DisplayName("알림 읽음 상태 변경에 성공한다.")
    void markAsRead_success() {
        // given
        given(review.getUser()).willReturn(user);
        Notification notification = Notification.create(review, "[누구누구님이] 리뷰를 좋아합니다.");
        assertThat(notification.isConfirmed()).isFalse(); // 변경 전

        // when
        notification.markAsRead();

        // then
        assertThat(notification.isConfirmed()).isTrue(); // 변경 후
    }
}
