package com.sprint.deokhugam.notification.domain.exception;

import com.sprint.deokhugam.common.exception.DeokhugamException;
import com.sprint.deokhugam.common.exception.ErrorCode;

public class NotificationException extends DeokhugamException {
    public NotificationException(NotificationErrorCode errorCode) {
        super(errorCode);
    }
}
