package com.sprint.deokhugam.common.config;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 로컬 저장소(type=local)에 저장된 이미지를 /api/images/** 로 내려준다.
 * S3 모드에서는 presigned URL 을 쓰므로 등록되지 않는다.
 */
@Configuration
@ConditionalOnProperty(name = "deokhugam.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalImageWebConfig implements WebMvcConfigurer {
    static final String URL_PATTERN = "/api/images/**"; // LocalStorage 의 URL_PATH 와 같아야 한다

    private final Path uploadDir;

    public LocalImageWebConfig(@Value("${deokhugam.storage.path}") String path) {
        this.uploadDir = Path.of(path).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(URL_PATTERN)
            .addResourceLocations(uploadDir.toUri().toString());
    }
}
