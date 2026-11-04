package com.sprint.deokhugam.book.domain.exception;

import com.sprint.deokhugam.common.exception.DeokhugamException;

public class BookException extends DeokhugamException {

    public BookException(BookErrorCode errorCode) {
        super(errorCode);
    }
}
