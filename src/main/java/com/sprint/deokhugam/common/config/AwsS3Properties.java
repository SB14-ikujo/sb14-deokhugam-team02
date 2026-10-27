package com.sprint.deokhugam.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "deokhugam.storage.s3")
public class AwsS3Properties {
    private String accessKey;
    private String secretKey;
    private String region;
    private String bucket;
    private long presignedUrlExpiration = 600; // 초 단위 (기본값: 10분)
}
