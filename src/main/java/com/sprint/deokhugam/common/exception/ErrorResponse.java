package com.sprint.deokhugam.common.exception;

import java.time.Instant;

public record ErrorResponse(
    Instant timestamp,
    int status,
    String message,
    String details
) {
    public static ErrorResponse of(ErrorCode code) {
        return of(code, null);
    }

    public static ErrorResponse of(ErrorCode type, String details) {
        return new ErrorResponse(
            Instant.now(),               // 에러가 난 시각
            type.getStatus().value(),    // HttpStatus → 숫자
            type.getMessage(),           // ErrorCode에 적어 둔 메시지
            details
        );
    }

}
