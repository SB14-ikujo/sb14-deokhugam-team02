package com.sprint.deokhugam.dashboard.domain.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum DashboardErrorCode implements ErrorCode {

    DASHBOARD_INVALID_CURSOR(HttpStatus.BAD_REQUEST, "잘못된 커서입니다.");

    private final HttpStatus status;
    private final String message;
}
