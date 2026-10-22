package com.sprint.deokhugam.notification.application.port.in;

import java.util.UUID;

import com.sprint.deokhugam.notification.adapter.in.web.dto.NotificationDto;
import com.sprint.deokhugam.review.domain.Review;

public interface NotificationUseCase {

    void createLikeNotification(Review review, String actorNickname);

    void createCommentNotification(Review review, String actorNickname);

    NotificationDto markAsRead(UUID notificationId, UUID userId, boolean updateRequest);
}
