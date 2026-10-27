package com.sprint.deokhugam.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

import com.sprint.deokhugam.book.adapter.out.exception.StorageException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionController {

    @ExceptionHandler(DeokhugamException.class)
    public ResponseEntity<ErrorResponse> handleDeokhugam(DeokhugamException ex) {
        ErrorCode code = ex.getErrorCode();
        log.warn("[{}] {}", code.name(), ex.getMessage());
        return ResponseEntity
            .status(code.getStatus())
            .body(ErrorResponse.of(code));
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,          // @Valid 검증 실패
        BindException.class,
        HandlerMethodValidationException.class,         // @RequestParam 등 파라미터 검증 실패
        MissingRequestHeaderException.class,            // Deokhugam-Request-User-ID 헤더 누락
        MissingServletRequestParameterException.class,  // 필수 쿼리 누락
        MethodArgumentTypeMismatchException.class,      // enum 오타, UUID 형식 오류
        HttpMessageNotReadableException.class           // JSON 형식 오류
    })
    public ResponseEntity<ErrorResponse> handleInvalidInput(Exception ex) {
        log.warn("[INVALID_INPUT] {}", ex.getMessage());
        CommonErrorCode code = CommonErrorCode.INVALID_INPUT;
        return ResponseEntity
            .status(code.getStatus())
            .body(ErrorResponse.of(code, fieldErrorOf(ex)));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex) {
        CommonErrorCode code = CommonErrorCode.RESOURCE_NOT_FOUND;
        return ResponseEntity
            .status(code.getStatus())
            .body(ErrorResponse.of(code));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        log.warn("[METHOD_NOT_ALLOWED] {}", ex.getMessage());
        CommonErrorCode code = CommonErrorCode.METHOD_NOT_ALLOWED;
        return ResponseEntity
            .status(code.getStatus())
            .body(ErrorResponse.of(code));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        log.warn("[UNSUPPORTED_MEDIA_TYPE] {}", ex.getMessage());
        CommonErrorCode code = CommonErrorCode.UNSUPPORTED_MEDIA_TYPE;
        return ResponseEntity
            .status(code.getStatus())
            .body(ErrorResponse.of(code));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleFileSizeExceeded(MaxUploadSizeExceededException ex) {
        log.warn("[FILE_SIZE_EXCEEDED] {}", ex.getMessage());
        CommonErrorCode code = CommonErrorCode.FILE_SIZE_EXCEEDED;
        return ResponseEntity
            .status(code.getStatus())
            .body(ErrorResponse.of(code));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleExceptionController(Exception ex) {
        log.error("[심각]", ex);
        return ResponseEntity
            .status(CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus())
            .body(ErrorResponse.of(CommonErrorCode.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorageException(StorageException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        log.error("Storage 예외 발생: {}", errorCode.getMessage(), ex);
        if (ex.getCause() instanceof AwsServiceException awsServiceException
            && awsServiceException.awsErrorDetails() != null) {
            log.error("AWS 에러코드: {}", awsServiceException.awsErrorDetails().errorCode());
        }

        return ResponseEntity
            .status(errorCode.getStatus())
            .body(ErrorResponse.of(errorCode));
    }

    // @Valid 실패면 "nickname: 2자 이상 20자 이하" 처럼 첫 번째 틀린 필드를 details에 담음
    private String fieldErrorOf(Exception ex) {
        BindingResult result = null;
        if (ex instanceof MethodArgumentNotValidException e) {
            result = e.getBindingResult();
        } else if (ex instanceof BindException e) {
            result = e.getBindingResult();
        }
        if (result != null && result.getFieldError() != null) {
            FieldError fe = result.getFieldError();
            return fe.getField() + ": " + fe.getDefaultMessage();
        }
        return null;
    }
}
