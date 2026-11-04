package com.sprint.deokhugam.common.exception;

import lombok.Getter;

@Getter
public abstract class DeokhugamException extends RuntimeException {

    private final ErrorCode errorCode;

    public DeokhugamException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public DeokhugamException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

}
