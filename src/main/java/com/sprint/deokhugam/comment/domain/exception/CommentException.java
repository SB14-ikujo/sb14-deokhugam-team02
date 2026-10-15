package com.sprint.deokhugam.comment.domain.exception;

import com.sprint.deokhugam.common.exception.DeokhugamException;

public class CommentException extends DeokhugamException {

    public CommentException(CommentErrorCode errorCode) {
        super(errorCode);
    }
}
