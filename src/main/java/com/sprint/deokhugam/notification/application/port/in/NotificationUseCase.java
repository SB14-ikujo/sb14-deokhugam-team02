package com.sprint.deokhugam.notification.application.port.in;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.notification.adapter.in.web.dto.NotificationDto;
import com.sprint.deokhugam.review.domain.Review;

public interface NotificationUseCase {
    // 좋아요 알림 생성
    void createLikeNotification(Review review, String actorNickname);

    // 댓글 알림 생성
    void createCommentNotification(Review review, String actorNickname);

    // 알림 단건 읽음 처리
    NotificationDto markAsRead(UUID notificationId, UUID userId, boolean confirmed);

    // 알림 전체 읽음 처리
    void markAllAsRead(UUID userId);

    // 알림 전체 조회(커서)
    CursorPageResponse<NotificationDto> getNotifications(
        UUID userId, Sort.Direction direction, Instant cursor,
        Instant after, Integer limit
    );

    // 리뷰 TOP10 알림 생성
    long createTop10ReviewNotifications(LocalDate baseDate);

    // 알림 삭제(읽음 처리 후 7일)
    long deleteReadNotifications(LocalDate baseDate);
}
