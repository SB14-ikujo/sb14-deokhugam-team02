package com.sprint.deokhugam.notification.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import com.sprint.deokhugam.notification.domain.exception.NotificationErrorCode;
import com.sprint.deokhugam.notification.domain.exception.NotificationException;

@Getter
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public enum NotificationType {
    PERIOD(),
    COMMENT(),
    LIKE();

    public static NotificationType from(String type) {
        if (type == null || type.isBlank()) {
            throw new NotificationException(NotificationErrorCode.NOTIFICATION_TYPE_INVALID);
        }
        for (NotificationType notificationType : NotificationType.values()) {
            if (notificationType.name().equals(type.toUpperCase())) {
                return notificationType;
            }
        }
        throw new NotificationException(NotificationErrorCode.NOTIFICATION_TYPE_INVALID);

    }
}
