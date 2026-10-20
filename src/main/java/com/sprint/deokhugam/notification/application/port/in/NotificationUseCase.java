package com.sprint.deokhugam.notification.application.port.in;

import com.sprint.deokhugam.notification.domain.Notification;
import com.sprint.deokhugam.notification.domain.NotificationMessageTemplate;
import com.sprint.deokhugam.review.domain.Review;

public interface NotificationUseCase {

    Notification createNotification(Review review, String actorNickname, NotificationMessageTemplate messageType);

}
