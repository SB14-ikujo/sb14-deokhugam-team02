package com.sprint.deokhugam.book.domain.exception;

import com.sprint.deokhugam.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BookErrorCode implements ErrorCode {

    BOOK_INVALID_ISBN(HttpStatus.BAD_REQUEST, "잘못된 ISBN 형식입니다."),
    BOOK_ISBN_DUPLICATE(HttpStatus.CONFLICT, "이미 등록된 ISBN입니다."),
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "도서 정보를 찾을 수 없습니다."),
    BOOK_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 ISBN의 도서 정보를 찾을 수 없습니다."),
    BOOK_OCR_FAILED(HttpStatus.BAD_REQUEST, "잘못된 이미지 형식이거나 ISBN을 인식하지 못했습니다.");

    private final HttpStatus status;
    private final String message;

}
