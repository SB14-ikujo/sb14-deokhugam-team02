package com.sprint.deokhugam.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConditionalOnProperty(name = "deokhugam.storage.type", havingValue = "s3")
@ConfigurationProperties(prefix = "deokhugam.storage.s3")
public class AwsS3Properties {
    private String accessKey;
    private String secretKey;
    private String region;
    private String bucket;
    private long presignedUrlExpiration = 600; // 초 단위 (기본값: 10분)
}
