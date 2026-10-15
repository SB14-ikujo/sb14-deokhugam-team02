package com.sprint.deokhugam.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionController {

    @ExceptionHandler(DeokhugamException.class)
    public ResponseEntity<ErrorResponse> handleDeokhugam(DeokhugamException e){
        ErrorCode code = e.getErrorCode();
        log.warn("[{}] {}", code.name(), e.getMessage());
        return ResponseEntity
            .status(code.getStatus())
            .body(ErrorResponse.of(code));
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,          // @Valid 검증 실패
        HandlerMethodValidationException.class,         // @RequestParam 등 파라미터 검증 실패
        MissingRequestHeaderException.class,            // Deokhugam-Request-User-ID 헤더 누락
        MissingServletRequestParameterException.class,  // 필수 쿼리 누락
        MethodArgumentTypeMismatchException.class,      // enum 오타, UUID 형식 오류
        HttpMessageNotReadableException.class           // JSON 형식 오류
    })
    public ResponseEntity<ErrorResponse> handleInvalidInput(Exception e) {
        log.warn("[INVALID_INPUT] {}", e.getMessage());
        CommonErrorCode code = CommonErrorCode.INVALID_INPUT;
        return ResponseEntity
            .status(code.getStatus())
            .body(ErrorResponse.of(code, fieldErrorOf(e)));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException e) {
        CommonErrorCode code = CommonErrorCode.RESOURCE_NOT_FOUND;
        return ResponseEntity
            .status(code.getStatus())
            .body(ErrorResponse.of(code));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleExceptionController(Exception e){
        log.error("[심각]", e);
        return ResponseEntity
            .status(CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus())
            .body(ErrorResponse.of(CommonErrorCode.INTERNAL_SERVER_ERROR));
    }


    // @Valid 실패면 "nickname: 2자 이상 20자 이하" 처럼 첫 번째 틀린 필드를 details에 담음
    private String fieldErrorOf(Exception e) {
        if (e instanceof MethodArgumentNotValidException ex) {
            FieldError fieldError = ex.getBindingResult().getFieldError();
            if (fieldError != null) {
                return fieldError.getField() + ": " + fieldError.getDefaultMessage();
            }
        }
        return null;
    }
}
