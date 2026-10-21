package com.sprint.deokhugam.common.context.user;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)       // 파라미터에서 적용 가능 어노테이션
@Retention(RetentionPolicy.RUNTIME)  // 런타임까지 유지
public @interface RequestUserId {
    boolean required() default true;
}
