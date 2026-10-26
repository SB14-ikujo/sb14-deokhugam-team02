package com.sprint.deokhugam.notification.application.port.in;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.dashboard.application.port.out.popularreview.PopularReviewRepositoryPort;
import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PopularReview;
import com.sprint.deokhugam.notification.adapter.in.web.dto.NotificationDto;
import com.sprint.deokhugam.notification.adapter.in.web.mapper.NotificationMapper;
import com.sprint.deokhugam.notification.application.port.out.NotificationRepositoryPort;
import com.sprint.deokhugam.notification.domain.Notification;
import com.sprint.deokhugam.notification.domain.exception.NotificationException;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.user.application.port.out.UserRepositoryPort;
import com.sprint.deokhugam.user.domain.User;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock
    private NotificationRepositoryPort notificationRepository;

    @Mock
    private PopularReviewRepositoryPort popularReviewRepository;

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private Review review;

    @Mock
    private PopularReview popularReview;

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant T2 = Instant.parse("2026-01-02T00:00:00Z");

    @Test
    @DisplayName("좋아요 알림 생성 시 메시지, 리뷰 정보 및 저장 호출을 검증한다.")
    void createLikeNotification_success() {
        // given
        String actorNickname = "코코";
        given(notificationRepository.save(any(Notification.class)))
            .willAnswer(invocation -> invocation.getArgument(0));   // 넘긴 객체 그대로 반환
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        // when
        notificationService.createLikeNotification(review, actorNickname);

        // then
        verify(notificationRepository).save(captor.capture());   // save 1번 호출 + 넘긴 인자 캡처
        Notification saved = captor.getValue();                  // 캡처한 객체 꺼내기
        assertThat(saved.getMessage()).isEqualTo("[코코]님이 나의 리뷰를 좋아합니다.");
        assertThat(saved.getReview()).isEqualTo(review);
    }

    @Test
    @DisplayName("댓글 알림 생성 시 메시지, 리뷰 정보 및 저장 호출을 검증한다.")
    void createCommentNotification_success() {
        // given
        given(notificationRepository.save(any(Notification.class)))
            .willAnswer(invocation -> invocation.getArgument(0));   // 넘긴 객체 그대로 반환
        String actorNickname = "코코";
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        // when
        notificationService.createCommentNotification(review, actorNickname);

        // then
        verify(notificationRepository).save(captor.capture());   // save 1번 호출 + 넘긴 인자 캡처
        Notification saved = captor.getValue();                  // 캡처한 객체 꺼내기
        assertThat(saved.getMessage()).isEqualTo("[코코]님이 나의 리뷰에 댓글을 남겼습니다.");
        assertThat(saved.getReview()).isEqualTo(review);

    }

    @Test
    @DisplayName("리뷰가 null이면 알림을 생성하지 않고 저장하지 않는다.")
    void createLikeNotification_nullReview() {
        // given
        String actorNickname = "코코";

        // when
        notificationService.createCommentNotification(null, actorNickname);

        // then
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @ParameterizedTest                  // 아래 소스들이 제공하는 값마다 테스트를 한 번씩 반복 실행 (@Test 대체)
    @NullAndEmptySource                 // null, "" 제공
    @ValueSource(strings = {" ", "\t"}) // 공백, 탭 제공 (isBlank 케이스)
    @DisplayName("알림 발생자 닉네임이 null 또는 빈 문자열이면 알림을 생성하지 않는다.")
    void createLikeNotification_nullOrEmptyActorNickname(String actorNickname) {
        // when
        notificationService.createCommentNotification(review, actorNickname);

        // then
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("존재하지 않는 알림 ID로 읽음 처리 시 예외가 발생한다.")
    void markAsRead_notificationNotFound() {
        UUID notificationId = UUID.randomUUID();
        UUID userId = UUID.fromString("50000000-0000-0000-0000-000000000002");

        // given
        given(notificationRepository.findById(any())).willReturn(Optional.empty());

        // when + then
        assertThrows(NotificationException.class, () -> notificationService.markAsRead(notificationId, userId, true));
    }

    @Test
    @DisplayName("알림 소유자와 요청 사용자가 일치하지 않으면 예외가 발생한다.")
    void markAsRead_notificationForbidden() {
        Notification notification = mock(Notification.class);
        User user = mock(User.class);
        UUID notificationId = UUID.randomUUID();
        UUID userId = UUID.fromString("50000000-0000-0000-0000-000000000002");

        // given
        given(user.getId()).willReturn(UUID.fromString("10000000-0000-0000-0000-000000000002"));
        given(notification.getUser()).willReturn(user);
        given(notificationRepository.findById(any())).willReturn(Optional.of(notification));

        // when + then
        assertThrows(NotificationException.class, () -> notificationService.markAsRead(notificationId, userId, true));
    }

    @Test
    @DisplayName("읽음 상태가 false이면 기존 알림 상태를 유지한다.")
    void markAsRead_readIsFalse() {
        Notification notification = mock(Notification.class);
        User user = mock(User.class);
        UUID notificationId = UUID.randomUUID();
        UUID userId = UUID.fromString("50000000-0000-0000-0000-000000000002");

        // given
        given(user.getId()).willReturn(UUID.fromString("50000000-0000-0000-0000-000000000002"));
        given(notification.getUser()).willReturn(user);
        given(notificationRepository.findById(any())).willReturn(Optional.of(notification));

        // when
        notificationService.markAsRead(notificationId, userId, false);

        // then
        verify(notification, never()).markAsRead();
        verify(notificationRepository).findById(any());
        verify(notificationMapper).toDto(any());
    }

    // 성공
    @Test
    @DisplayName("본인의 알림을 읽음 상태로 변경하고 알림 정보를 반환한다.")
    void markAsRead_success() {
        Notification notification = mock(Notification.class);
        User user = mock(User.class);
        UUID notificationId = UUID.randomUUID();
        UUID userId = UUID.fromString("50000000-0000-0000-0000-000000000002");

        // given
        given(user.getId()).willReturn(UUID.fromString("50000000-0000-0000-0000-000000000002"));
        given(notification.getUser()).willReturn(user);
        given(notificationRepository.findById(any())).willReturn(Optional.of(notification));

        // when
        notificationService.markAsRead(notificationId, userId, true);

        // then
        verify(notificationRepository).findById(any());
        verify(notificationMapper).toDto(any());
    }

    @Test
    @DisplayName("사용자의 알림이 존재하지 않으면 NotificationException이 발생한다.")
    void markAllAsRead_failsWhenNotificationsEmpty() {
        UUID userId = UUID.fromString("50000000-0000-0000-0000-000000000002");

        // given
        given(notificationRepository.existsByUserId(userId)).willReturn(false);

        // when + then
        assertThrows(NotificationException.class, () -> notificationService.markAllAsRead(userId));
    }

    @Test
    @DisplayName("사용자의 알림이 존재하면 전체 읽음 처리에 성공한다.")
    void markAllAsRead_succeedsWhenNotificationsExist() {
        UUID userId = UUID.fromString("50000000-0000-0000-0000-000000000002");
        // given
        given(notificationRepository.existsByUserId(userId)).willReturn(true);

        // when
        notificationService.markAllAsRead(userId);

        // then
        verify(notificationRepository).markAllAsRead(userId);
    }

    @Test
    @DisplayName("사용자가 존재하지 않으면 NotificationException이 발생하고 알림 조회를 수행하지 않는다.")
    void getNotifications_failsWhenUserNotFound() {
        UUID userId = UUID.fromString("50000000-0000-0000-0000-000000000002");
        // given
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when, then
        assertThrows(NotificationException.class, () -> notificationService.getNotifications(
            userId,
            null,
            null,
            null,
            null
        ));
    }

    @Test
    @DisplayName("다음 페이지가 존재하면 hasNext는 true이고 마지막 항목의 생성일을 nextCursor로 반환한다.")
    void getNotifications_succeedsWhenNextPageExists() {
        // given
        int limit = 2;
        UUID userId = UUID.fromString("50000000-0000-0000-0000-000000000002");
        Instant t1 = Instant.parse("2026-01-01T00:00:00Z");
        Instant t2 = Instant.parse("2026-01-02T00:00:00Z");
        Instant t3 = Instant.parse("2026-01-03T00:00:00Z");

        User user = mock(User.class);
        Notification n1 = mock(Notification.class);
        Notification n2 = mock(Notification.class);
        Notification n3 = mock(Notification.class);
        NotificationDto dto1 = createDto(t1);
        NotificationDto dto2 = createDto(t2);
        NotificationDto dto3 = createDto(t3);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(notificationRepository.countByUserId(userId)).willReturn(10L);
        given(notificationRepository.findAllByUserIdWithCursor(any(), any(), any(), any(), eq(limit)))
            .willReturn(List.of(n1, n2, n3));
        given(notificationMapper.toDto(n1)).willReturn(dto1);
        given(notificationMapper.toDto(n2)).willReturn(dto2);
        given(notificationMapper.toDto(n3)).willReturn(dto3);

        // when
        CursorPageResponse<NotificationDto> result =
            notificationService.getNotifications(userId, Sort.Direction.DESC, null, null, limit);

        // then
        assertThat(result.hasNext()).isTrue();
        assertThat(result.content()).containsExactly(dto1, dto2);
        assertThat(result.nextCursor()).isEqualTo(t2.toString());
        assertThat(result.nextAfter()).isEqualTo(t2);
        assertThat(result.size()).isEqualTo(limit);
        assertThat(result.totalElements()).isEqualTo(10L);
    }

    @Test
    @DisplayName("다음 페이지가 존재하지 않으면 hasNext는 false이고 nextCursor는 null을 반환한다.")
    void getNotifications_succeedsWhenNextPageNotExists() {
        // given
        int limit = 2;
        UUID userId = UUID.fromString("50000000-0000-0000-0000-000000000002");
        Instant t1 = Instant.parse("2026-01-01T00:00:00Z");
        Instant t2 = Instant.parse("2026-01-02T00:00:00Z");

        User user = mock(User.class);
        Notification n1 = mock(Notification.class);
        Notification n2 = mock(Notification.class);
        Notification n3 = mock(Notification.class);
        NotificationDto dto1 = createDto(t1);
        NotificationDto dto2 = createDto(t2);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(notificationRepository.countByUserId(userId)).willReturn(2L);
        given(notificationRepository.findAllByUserIdWithCursor(any(), any(), any(), any(), eq(limit)))
            .willReturn(List.of(n1, n2));
        given(notificationMapper.toDto(n1)).willReturn(dto1);
        given(notificationMapper.toDto(n2)).willReturn(dto2);

        // when
        CursorPageResponse<NotificationDto> result =
            notificationService.getNotifications(userId, Sort.Direction.DESC, null, null, limit);

        // then
        assertThat(result.hasNext()).isFalse();
        assertThat(result.content()).containsExactly(dto1, dto2);
        assertThat(result.nextCursor()).isNull();
        assertThat(result.nextAfter()).isNull();
        assertThat(result.size()).isEqualTo(limit);
        assertThat(result.totalElements()).isEqualTo(2L);
    }

    @Test
    @DisplayName("기간별 인기 리뷰 TOP 10 알림을 생성한다")
    void createTop10ReviewNotifications_success() {
        // given: 2026-10-28(수) → DAILY만 대상
        LocalDate baseDate = LocalDate.of(2026, 10, 28);
        Review review = mock(Review.class);
        given(review.getContent()).willReturn("123456789");
        PopularReview popular = mock(PopularReview.class);
        given(popular.getReview()).willReturn(review);
        given(popular.getPeriod()).willReturn(PeriodType.DAILY);
        given(popularReviewRepository.findTop10ByPeriod(PeriodType.DAILY, baseDate))
            .willReturn(List.of(popular));
        given(notificationRepository.saveAll(anyList()))
            .willAnswer(inv -> inv.getArgument(0));

        // when
        long created = notificationService.createTop10ReviewNotifications(baseDate);

        // then
        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());
        assertThat(created).isEqualTo(1);
        assertThat(captor.getValue()).extracting(Notification::getMessage).allMatch(m -> m.contains("12345..."));
        verify(popularReviewRepository, never()).findTop10ByPeriod(eq(PeriodType.WEEKLY), any());
    }

    @Test
    @DisplayName("지정된 날짜 기준으로 읽은 알림을 삭제한다")
    void deleteReadNotifications_success() {
        // given
        LocalDate baseDate = LocalDate.of(2026, 10, 18);
        // when
        notificationService.deleteReadNotifications(baseDate);
        // then
        verify(notificationRepository).deleteReadNotificationsBefore(baseDate);

    }

    private NotificationDto createDto(Instant createdAt) {
        return new NotificationDto(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            "리뷰 내용", "알림 메시지", false, createdAt, createdAt
        );
    }
}
