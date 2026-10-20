package com.sprint.deokhugam.notification.application.port.in;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.notification.application.port.out.NotificationRepositoryPort;
import com.sprint.deokhugam.notification.domain.Notification;
import com.sprint.deokhugam.notification.domain.NotificationMessageTemplate;
import com.sprint.deokhugam.notification.domain.NotificationType;
import com.sprint.deokhugam.review.domain.Review;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationUseCase {
    private final NotificationRepositoryPort notificationRepository;

    @Override
    public void createLikeNotification(Review review, String actorNickname) {
        this.createNotification(review, actorNickname, NotificationType.LIKE);
    }

    @Override
    public void createCommentNotification(Review review, String actorNickname) {
        this.createNotification(review, actorNickname, NotificationType.COMMENT);
    }

    private void createNotification(Review review, String actorNickname, NotificationType type) {

        if (review == null) {
            log.warn("알림 생성 생략: 리뷰 정보가 없습니다. actorNickname={}", actorNickname);
            return;
        }

        if (actorNickname == null || actorNickname.isBlank()) {
            log.warn("알림 생성 생략: 알림 발생자 닉네임이 유효하지 않습니다. reviewId={}, actorNickname={}", review.getId(), actorNickname);
            return;
        }

        String message = this.getNotificationMessageTemplate(type).createMessageTemplate(actorNickname);
        Notification createdNotification = Notification.create(review, message);
        notificationRepository.save(createdNotification);
    }

    private NotificationMessageTemplate getNotificationMessageTemplate(NotificationType type) {
        return switch (type) {
            case LIKE -> NotificationMessageTemplate.LIKE;
            case COMMENT -> NotificationMessageTemplate.COMMENT;
        };
    }
}
