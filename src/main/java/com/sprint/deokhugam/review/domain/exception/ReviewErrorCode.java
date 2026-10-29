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
    REVIEW_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 리뷰입니다."),
    INVALID_REVIEW_CONTENT(HttpStatus.BAD_REQUEST, "리뷰 내용은 비어있을 수 없습니다."),
    INVALID_REVIEW_RATING(HttpStatus.BAD_REQUEST, "리뷰 평점은 1점에서 5점 사이여야 합니다."),
    REVIEW_INVALID_CURSOR(HttpStatus.BAD_REQUEST, "올바르지 않은 리뷰 평점 점수입니다.");

    private final HttpStatus status;
    private final String message;
}
