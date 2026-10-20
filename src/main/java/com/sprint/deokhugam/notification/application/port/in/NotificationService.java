package com.sprint.deokhugam.notification.application.port.in;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.notification.application.port.out.NotificationRepositoryPort;
import com.sprint.deokhugam.notification.domain.Notification;
import com.sprint.deokhugam.notification.domain.NotificationMessageTemplate;
import com.sprint.deokhugam.notification.domain.NotificationType;
import com.sprint.deokhugam.review.domain.Review;

@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationUseCase {
    private final NotificationRepositoryPort notificationRepository;

    @Override
    public Notification createLikeNotification(Review review, String actorNickname) {
        return this.createNotification(review, actorNickname, NotificationType.LIKE);
    }

    @Override
    public Notification createCommentNotification(Review review, String actorNickname) {
        return this.createNotification(review, actorNickname, NotificationType.COMMENT);
    }

    private Notification createNotification(Review review, String actorNickname, NotificationType type) {
        String message = NotificationMessageTemplate.LIKE.createMessageTemplate(actorNickname);
        if (type == NotificationType.COMMENT) {
            message = NotificationMessageTemplate.COMMENT.createMessageTemplate(actorNickname);
        }

        Notification createdNotification = Notification.create(review, message);

        return notificationRepository.save(createdNotification);
    }
}
