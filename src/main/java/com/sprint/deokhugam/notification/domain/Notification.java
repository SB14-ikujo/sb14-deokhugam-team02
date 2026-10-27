package com.sprint.deokhugam.notification.domain;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.sprint.deokhugam.common.domain.BaseEntity;
import com.sprint.deokhugam.notification.domain.exception.NotificationErrorCode;
import com.sprint.deokhugam.notification.domain.exception.NotificationException;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.user.domain.User;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)      // 알림 받는 사람
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Review review;

    @Column(nullable = false)
    private String message;     // 메세지

    @ColumnDefault("false")
    @Column(nullable = false)
    private boolean confirmed;  // 확인여부

    private Notification(User user, Review review, String message) {
        this.user = user;
        this.review = review;
        this.message = message;
    }

    public static Notification create(Review review, String message) {
        if (review == null) {
            throw new NotificationException(NotificationErrorCode.NOTIFICATION_REVIEW_NOT_FOUND);
        }

        if (message == null || message.isBlank()) {
            throw new NotificationException(NotificationErrorCode.NOTIFICATION_MESSAGE_REQUIRED);
        }

        return new Notification(review.getUser(), review, message);
    }

    public void markAsRead() {
        this.confirmed = true;
    }
}
