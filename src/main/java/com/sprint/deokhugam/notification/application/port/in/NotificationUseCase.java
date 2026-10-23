package com.sprint.deokhugam.notification.application.port.in;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.notification.adapter.in.web.dto.NotificationDto;
import com.sprint.deokhugam.review.domain.Review;

public interface NotificationUseCase {

    void createLikeNotification(Review review, String actorNickname);

    void createCommentNotification(Review review, String actorNickname);

    NotificationDto markAsRead(UUID notificationId, UUID userId, boolean confirmed);

    void markAllAsRead(UUID userId);

    CursorPageResponse<NotificationDto> getNotifications(
        UUID userId, Sort.Direction direction, String cursor,
        Instant after, Integer limit
    );
}
