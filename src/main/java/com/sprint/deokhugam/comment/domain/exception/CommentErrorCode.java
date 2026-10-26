package com.sprint.deokhugam.comment.domain.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum CommentErrorCode implements ErrorCode {
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글 정보를 찾을 수 없습니다."),
    COMMENT_REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰 정보를 찾을 수 없습니다."),
    COMMENT_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."),
    COMMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "댓글 수정·삭제 권한이 없습니다."),
    COMMENT_INVALID_CURSOR(HttpStatus.BAD_REQUEST, "잘못된 Cursor 값입니다.");

    private final HttpStatus status;
    private final String message;
}
