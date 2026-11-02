package com.sprint.deokhugam.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "deokhugam.ocr")
public class OcrSpaceProperties {
    private String apiKey;
    private String url;
    private int timeoutSeconds = 8; // 프론트 axios 타임아웃(10초)보다 짧게
}
