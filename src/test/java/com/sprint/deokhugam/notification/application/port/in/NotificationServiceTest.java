package com.sprint.deokhugam.notification.application.port.in;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sprint.deokhugam.notification.application.port.out.NotificationRepositoryPort;
import com.sprint.deokhugam.notification.domain.Notification;
import com.sprint.deokhugam.review.domain.Review;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock
    private NotificationRepositoryPort notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private Review review;

    @Test
    @DisplayName("좋아요 알림 생성 시 메시지, 리뷰 정보 및 저장 호출을 검증한다.")
    void createLikeNotification_success() {
        // given
        given(notificationRepository.save(any(Notification.class)))
            .willAnswer(invocation -> invocation.getArgument(0));   // 넘긴 객체 그대로 반환

        String actorNickname = "코코";
        // when
        Notification notification = notificationService.createLikeNotification(review, actorNickname);

        // then
        assertThat(notification.getMessage()).isEqualTo("[코코]님이 나의 리뷰를 좋아합니다.");
        assertThat(notification.getReview()).isEqualTo(review);
        verify(notificationRepository).save(any(Notification.class)); // save 한번 호출 되었는지
    }

    @Test
    @DisplayName("댓글 알림 생성 시 메시지, 리뷰 정보 및 저장 호출을 검증한다.")
    void createCommentNotification_success() {
        // given
        given(notificationRepository.save(any(Notification.class)))
            .willAnswer(invocation -> invocation.getArgument(0));   // 넘긴 객체 그대로 반환
        String actorNickname = "코코";

        // when
        Notification notification = notificationService.createCommentNotification(review, actorNickname);

        // then
        assertThat(notification.getMessage()).isEqualTo("[코코]님이 나의 리뷰에 댓글을 남겼습니다.");
        assertThat(notification.getReview()).isEqualTo(review);
        verify(notificationRepository).save(any(Notification.class)); // save 한번 호출 되었는지
    }

    @Test
    @DisplayName("리뷰가 null이면 알림을 생성하지 않고 저장하지 않는다.")
    void createLikeNotification_nullReview() {
        // given
        String actorNickname = "코코";

        // when
        Notification notification = notificationService.createCommentNotification(null, actorNickname);

        // then
        assertThat(notification).isNull();
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @ParameterizedTest                  // 아래 소스들이 제공하는 값마다 테스트를 한 번씩 반복 실행 (@Test 대체)
    @NullAndEmptySource                 // null, "" 제공
    @ValueSource(strings = {" ", "\t"}) // 공백, 탭 제공 (isBlank 케이스)
    @DisplayName("알림 발생자 닉네임이 null 또는 빈 문자열이면 알림을 생성하지 않는다.")
    void createLikeNotification_nullOrEmptyActorNickname(String actorNickname) {
        // when
        Notification notification = notificationService.createCommentNotification(review, actorNickname);

        // then
        assertThat(notification).isNull();
        verify(notificationRepository, never()).save(any(Notification.class));
    }

}
