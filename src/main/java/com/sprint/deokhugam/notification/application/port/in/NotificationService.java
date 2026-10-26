package com.sprint.deokhugam.notification.application.port.in;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.common.util.SliceUtils;
import com.sprint.deokhugam.dashboard.application.port.out.popularreview.PopularReviewRepositoryPort;
import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PopularReview;
import com.sprint.deokhugam.notification.adapter.in.web.dto.NotificationDto;
import com.sprint.deokhugam.notification.adapter.in.web.mapper.NotificationMapper;
import com.sprint.deokhugam.notification.application.port.out.NotificationRepositoryPort;
import com.sprint.deokhugam.notification.domain.Notification;
import com.sprint.deokhugam.notification.domain.NotificationMessageTemplate;
import com.sprint.deokhugam.notification.domain.NotificationType;
import com.sprint.deokhugam.notification.domain.exception.NotificationErrorCode;
import com.sprint.deokhugam.notification.domain.exception.NotificationException;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.user.application.port.out.UserRepositoryPort;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationUseCase {
    private final UserRepositoryPort userRepository;
    private final NotificationRepositoryPort notificationRepository;
    private final PopularReviewRepositoryPort popularReviewRepository;
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
        if (!notificationRepository.existsByUserId(userId)) {
            log.debug("요청한 유저 알림 존재하지 않음 userId : {}", userId);
            throw new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
        }

        long updatedCount = notificationRepository.markAllAsRead(userId);
        log.info("user : {}의 알림 {}건 읽음 처리 완료", userId, updatedCount);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<NotificationDto> getNotifications(
        UUID userId, Sort.Direction direction, Instant cursor,
        Instant after, Integer limit
    ) {
        log.debug("user : {} 알림 목록 조회 ", userId);
        log.debug("페이지네이션 정보 [cursor: {}, direction: {}, after: {}, limit: {}]", cursor, direction, after, limit);
        userRepository.findById(userId)
            .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_USER_NOT_FOUND));

        long notificationTotalCount = notificationRepository.countByUserId(userId);

        List<NotificationDto> notifications = notificationRepository
            .findAllByUserIdWithCursor(userId, direction, cursor, after, limit)
            .stream().map(notificationMapper::toDto)
            .toList();

        Slice<NotificationDto> slice = SliceUtils.toSlice(notifications, limit);

        log.debug("총 데이터 : {}", notificationTotalCount);
        log.info(
            "최종 데이터 정보 [content_size: {}, limit: {}, hasNext : {}]",
            slice.getNumberOfElements(), slice.getSize(), slice.hasNext()
        );

        return CursorPageResponse.of(
            slice, notificationTotalCount,
            dto -> dto.createdAt().toString(),
            NotificationDto::createdAt
        );
    }

    @Override
    @Transactional
    public long createTop10ReviewNotifications(LocalDate baseDate) {
        List<Notification> notifications = new ArrayList<>();

        for (PeriodType period : PeriodType.values()) {
            if (!period.isNotifyDay(baseDate)) {
                continue;
            }
            List<PopularReview> top = popularReviewRepository.findTop10ByPeriod(period, baseDate);
            top.forEach(popularReview -> notifications.add(toNotification(popularReview)));
            log.info("인기 리뷰 알림 생성: period={}, count={}", period, top.size());
        }
        return notificationRepository.saveAll(notifications).size();
    }

    @Override
    @Transactional
    public long deleteReadNotifications(LocalDate baseDate) {
        return notificationRepository.deleteReadNotificationsBefore(baseDate);
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
            case PERIOD -> NotificationMessageTemplate.BEST_REVIEW;
        };
    }

    private Notification toNotification(PopularReview popularReview) {
        log.debug("인기 리뷰 알림 생성 대상 리뷰 ID: {} , Rank: {}, 기간 :{}",
            popularReview.getReview().getId(), popularReview.getRank(), popularReview.getPeriod());

        String reviewContent = popularReview.getReview().getContent();
        String truncatedReviewContent = reviewContent.length() > 5
            ? reviewContent.substring(0, 5) + "..."
            : reviewContent;
        String savedMessage = this.getNotificationMessageTemplate(NotificationType.PERIOD)
            .createMessageTemplate(truncatedReviewContent, popularReview.getPeriod());
        return Notification.create(popularReview.getReview(), savedMessage);
    }
}
