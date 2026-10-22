package com.sprint.deokhugam.review.domain.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ReviewLikeErrorCode implements ErrorCode {
    REVIEW_AND_USER_NOT_FOUNT(HttpStatus.NOT_FOUND, "좋아요 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
