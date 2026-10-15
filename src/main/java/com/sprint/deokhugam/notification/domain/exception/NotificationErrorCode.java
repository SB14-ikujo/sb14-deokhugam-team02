package com.sprint.deokhugam.notification.domain.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {

    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림 정보를 찾을 수 없습니다."),
    NOTIFICATION_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."),
    NOTIFICATION_FORBIDDEN(HttpStatus.FORBIDDEN, "알림 수정 권한이 없습니다.");

    private final HttpStatus status;
    private final String message;
}
