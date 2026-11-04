package com.sprint.deokhugam.common.util;

import java.util.Locale;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/*
 * 저장된 이미지 키의 확장자를 기준으로 MIME 타입을 판별한다.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContentTypeUtils {
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    public static String resolve(String fileName) {
        int extensionIndex = fileName.lastIndexOf('.');
        if (extensionIndex == -1) {
            return DEFAULT_CONTENT_TYPE;
        }

        String extension = fileName.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);

        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            default -> DEFAULT_CONTENT_TYPE;
        };
    }

}
