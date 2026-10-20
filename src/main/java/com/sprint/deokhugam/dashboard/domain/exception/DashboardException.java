package com.sprint.deokhugam.dashboard.domain.exception;

import com.sprint.deokhugam.common.exception.DeokhugamException;
import com.sprint.deokhugam.common.exception.ErrorCode;

public class DashboardException extends DeokhugamException {

    public DashboardException(ErrorCode errorCode) {
        super(errorCode);
    }
}
