package com.sprint.deokhugam.notification.domain.exception;

import com.sprint.deokhugam.common.exception.DeokhugamException;

public class NotificationException extends DeokhugamException {
    public NotificationException(NotificationErrorCode errorCode) {
        super(errorCode);
    }
}
