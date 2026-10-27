package com.sprint.deokhugam.book.adapter.out.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum StorageErrorCode implements ErrorCode {

    // AWS - S3
    S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다."),
    S3_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제에 실패했습니다."),
    S3_URL_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 URL 생성에 실패했습니다."),
    S3_CONNECTION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "스토리지 서버에 연결할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

}
