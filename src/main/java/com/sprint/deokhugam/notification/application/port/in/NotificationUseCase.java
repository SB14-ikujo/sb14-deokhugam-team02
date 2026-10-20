package com.sprint.deokhugam.notification.application.port.in;

import com.sprint.deokhugam.notification.domain.Notification;
import com.sprint.deokhugam.review.domain.Review;

public interface NotificationUseCase {

    Notification createLikeNotification(Review review, String actorNickname);

    Notification createCommentNotification(Review review, String actorNickname);
}
