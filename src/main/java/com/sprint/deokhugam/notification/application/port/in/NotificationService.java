package com.sprint.deokhugam.notification.application.port.in;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.notification.application.port.out.NotificationRepositoryPort;
import com.sprint.deokhugam.notification.domain.Notification;
import com.sprint.deokhugam.notification.domain.NotificationType;
import com.sprint.deokhugam.review.domain.Review;

@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationUseCase {
    private final NotificationRepositoryPort notificationRepository;

    @Override
    public Notification createNotification(Review review, String actorNickname, NotificationType type) {
        String message = type.createMessageTemplate(actorNickname);
        Notification createdNotification = Notification.create(review, message);

        return notificationRepository.save(createdNotification);

    }

}
