package com.sprint.deokhugam.common.logging;

import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.common.context.UserContextHolder;
import com.sprint.deokhugam.user.domain.exception.UserErrorCode;
import com.sprint.deokhugam.user.domain.exception.UserException;

/**
 * HandlerInterceptor : 컨트롤러로 들어오는 요청에 대한 전/후 처리를 가능하게 해주는 인터페이스
 * preHandle: 컨트롤러 호출 전에 호출되는 메서드 (false로 반환되면 다음 차례 컨트롤러 미실행)
 * postHandle: 컨트롤러가 정상적으로 실행된 이후에 실행 (컨트롤러에서 예외 발생 시 미실행)
 * afterCompletion: 컨트롤러가 응답을 전송한 뒤에 실행되는 메서드 (컨트롤러에서 예외발생 시 4번째 파라미터로 전달되어 로그를 남기는 후처리 가능)
 */
@Slf4j
@Component
public class MdcLoggingInterceptor implements HandlerInterceptor {
    public static final String USER_ID_HEADER = "Deokhugam-Request-User-ID";
    public static final String REQUEST_IP = "requestedIp";
    public static final String REQUEST_ID = "requestedUserId";
    public static final String REQUEST_METHOD = "requestMethod";
    public static final String REQUEST_URL = "requestUrl";

    @Override
    public boolean preHandle(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler
    ) {
        String authHeader = request.getHeader(USER_ID_HEADER);
        MDC.put(REQUEST_IP, request.getRemoteAddr());
        MDC.put(REQUEST_METHOD, request.getMethod());
        MDC.put(REQUEST_URL, request.getRequestURI());

        if (authHeader != null && !authHeader.isBlank()) {
            try {
                UUID userId = UUID.fromString(authHeader);
                UserContextHolder.setUserId(userId);
                MDC.put(REQUEST_ID, userId.toString());
            } catch (IllegalArgumentException e) {
                MDC.clear();
                throw new UserException(UserErrorCode.USER_INVALID_HEADER);
            }
        }

        return true;
    }

    @Override
    public void afterCompletion( // 스레드 오염방지를 위한 값 정리
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler,
        Exception ex
    ) {
        MDC.clear();
        UserContextHolder.clear();
    }
}
