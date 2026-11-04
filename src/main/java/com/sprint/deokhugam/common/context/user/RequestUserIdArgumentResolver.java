package com.sprint.deokhugam.common.context.user;

import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * supportsParameter : 주어진 메서드의 파라미터가 지원하는 타입인지 여부를 검사
 * resolveArgument   : 이 메서드의 반환값이 대상이 되는 메서드의 파라미터에 바인딩
 */
public class RequestUserIdArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestUserId.class)    // RequestUserId 어노테이션 붙었는지 체크
            && parameter.getParameterType().equals(UUID.class);         // Type이 UUID가 맞는지 체크
    }

    @Override
    public @Nullable Object resolveArgument(
        @NonNull MethodParameter parameter,
        @Nullable ModelAndViewContainer mavContainer,
        @NonNull NativeWebRequest webRequest,
        @Nullable WebDataBinderFactory binderFactory
    ) throws Exception {
        UUID userId = UserContextHolder.getUserId();
        RequestUserId annotation = parameter.getParameterAnnotation(RequestUserId.class);

        if (userId == null && annotation != null && annotation.required()) {
            throw new MissingRequestHeaderException("Deokhugam-Request-User-ID", parameter);
        }
        return userId;
    }
}
