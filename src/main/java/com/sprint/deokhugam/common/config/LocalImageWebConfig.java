package com.sprint.deokhugam.common.config;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "deokhugam.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalImageWebConfig implements WebMvcConfigurer {
    private final Path uploadDir;

    public LocalImageWebConfig(@Value("${deokhugam.storage.path:./uploads}") String path) {
        this.uploadDir = Path.of(path).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api/images/**")
            .addResourceLocations(uploadDir.toUri().toString());
    }
}
