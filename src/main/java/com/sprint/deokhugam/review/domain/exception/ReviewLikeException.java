package com.sprint.deokhugam.review.domain.exception;

import com.sprint.deokhugam.common.exception.DeokhugamException;

public class ReviewLikeException extends DeokhugamException {
    public ReviewLikeException(ReviewLikeErrorCode errorCode) {
        super(errorCode);
    }
}
