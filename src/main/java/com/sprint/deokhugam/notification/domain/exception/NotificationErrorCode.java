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
    NOTIFICATION_FORBIDDEN(HttpStatus.FORBIDDEN, "알림 수정 권한이 없습니다."),
    NOTIFICATION_REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "알림과 관련된 리뷰를 찾을 수 없습니다."),
    NOTIFICATION_MESSAGE_REQUIRED(HttpStatus.BAD_REQUEST, "알림 메시지는 필수입니다."),
    NOTIFICATION_TYPE_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 알림 유형입니다.");

    private final HttpStatus status;
    private final String message;
}
