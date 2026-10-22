package com.sprint.deokhugam.notification.application.port.in;

import java.util.List;
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
    public NotificationDto markAsRead(UUID notificationId, UUID userId, boolean confirmed) {
        log.debug("알람 읽음 요청 정보 : 알림 아이디 : {}, 요청자 아이디 : {}, confirmed :{}", notificationId, userId, confirmed);

        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUser().getId().equals(userId)) {
            log.debug("알람 읽음 요청 정보 불일치 : 알림(userId) : {}, 요청자 아이디 : {}", notification.getUser().getId(), userId);
            throw new NotificationException(NotificationErrorCode.NOTIFICATION_FORBIDDEN);
        }

        if (!confirmed) {
            log.debug("confirmed false로 알람 읽음 처리 스킵");
            return notificationMapper.toDto(notification);
        }

        notification.markAsRead();

        return notificationMapper.toDto(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        List<Notification> notifications = notificationRepository.findByUserId(userId);
        log.debug("조회된 알림 개수 : {}", notifications.size());

        if (notifications.isEmpty()) {
            log.debug("요청한 유저 알림 존재하지 않음 userId : {}", userId);
            throw new NotificationException(NotificationErrorCode.NOTIFICATION_USER_NOT_FOUND);
        }

        notificationRepository.markAllAsRead(userId);
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

        log.debug("알람 생성 요청 정보 : 리뷰 아이디 : {}, 알림 발생자 : {}", review.getId(), actorNickname);

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
