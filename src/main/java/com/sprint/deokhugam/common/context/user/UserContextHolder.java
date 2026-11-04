package com.sprint.deokhugam.common.context.user;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserContextHolder {
    private static final ThreadLocal<UUID> USER_CONTEXT = new ThreadLocal<>();

    // 사용자 ID 저장
    public static void setUserId(UUID userId) {
        USER_CONTEXT.set(userId);
    }

    // 사용자 ID 조회
    public static UUID getUserId() {
        return USER_CONTEXT.get();
    }

    // 사용자 ID 제거 - 스레드 재사용 시 이전 요청 값이 남지 않도록
    public static void clear() {
        USER_CONTEXT.remove();
    }
}
