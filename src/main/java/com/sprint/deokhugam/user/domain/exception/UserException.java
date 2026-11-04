package com.sprint.deokhugam.user.domain.exception;

import com.sprint.deokhugam.common.exception.DeokhugamException;

public class UserException extends DeokhugamException {
    public UserException(UserErrorCode errorCode) {
        super(errorCode);
    }
}
