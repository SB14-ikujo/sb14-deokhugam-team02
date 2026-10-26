package com.sprint.deokhugam.review.domain.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements ErrorCode {
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰 정보를 찾을 수 없습니다."),
    REVIEW_BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "도서 정보를 찾을 수 없습니다."),
    REVIEW_DUPLICATE(HttpStatus.CONFLICT, "이미 작성된 리뷰가 존재합니다."),
    REVIEW_FORBIDDEN(HttpStatus.FORBIDDEN, "리뷰 수정·삭제 권한이 없습니다."),
    REVIEW_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 리뷰입니다.");

    private final HttpStatus status;
    private final String message;
}
