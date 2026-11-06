package com.sprint.deokhugam.book.adapter.out.storage.exception;

import com.sprint.deokhugam.common.exception.DeokhugamException;

public class StorageException extends DeokhugamException {

    public StorageException(StorageErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
