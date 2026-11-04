package com.sprint.deokhugam.review.domain.exception;

import com.sprint.deokhugam.common.exception.DeokhugamException;

public class ReviewException extends DeokhugamException {
    public ReviewException(ReviewErrorCode errorCode) {
        super(errorCode);
    }
}
