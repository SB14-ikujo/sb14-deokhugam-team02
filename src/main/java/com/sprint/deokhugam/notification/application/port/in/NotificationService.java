package com.sprint.deokhugam.notification.application.port.in;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.notification.adapter.in.web.dto.NotificationDto;
import com.sprint.deokhugam.notification.adapter.in.web.mapper.NotificationMapper;
import com.sprint.deokhugam.notification.application.port.out.NotificationRepositoryPort;
import com.sprint.deokhugam.notification.domain.Notification;
import com.sprint.deokhugam.notification.domain.NotificationMessageTemplate;
import com.sprint.deokhugam.notification.domain.NotificationType;
import com.sprint.deokhugam.notification.domain.exception.NotificationErrorCode;
import com.sprint.deokhugam.notification.domain.exception.NotificationException;
import com.sprint.deokhugam.review.domain.Review;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationUseCase {
    private final NotificationRepositoryPort notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public void createLikeNotification(Review review, String actorNickname) {
        this.createNotification(review, actorNickname, NotificationType.LIKE);
    }

    @Override
    public void createCommentNotification(Review review, String actorNickname) {
        this.createNotification(review, actorNickname, NotificationType.COMMENT);
    }

    @Override
    @Transactional
    public NotificationDto markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUser().getId().equals(userId)) {
            throw new NotificationException(NotificationErrorCode.NOTIFICATION_FORBIDDEN);
        }

        notification.markAsRead();
        Notification saved = notificationRepository.save(notification);   // 명시적 저장

        return notificationMapper.toDto(saved);
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
