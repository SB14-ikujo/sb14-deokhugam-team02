package com.sprint.deokhugam.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@ConditionalOnProperty(name = "deokhugam.storage.type", havingValue = "s3")
public class AwsS3Config {
    @Bean // S3Client 인터페이스 구현체 Bean 등록 (파일 업로드/다운로드/삭제용)
    public S3Client s3Client(AwsS3Properties properties) {
        return S3Client.builder()
            .region(Region.of(properties.getRegion()))
            .credentialsProvider(credentialsProvider(properties))
            .build();
    }

    @Bean // S3Presigner 인터페이스 구현체 Bean 등록 (Presigned URL(임시공개URL)생성용)
    public S3Presigner s3Presigner(AwsS3Properties properties) {
        return S3Presigner.builder()
            .region(Region.of(properties.getRegion()))
            .credentialsProvider(credentialsProvider(properties))
            .build();
    }

    // access key + secret key로 AWS 인증 정보 생성 (S3Client, S3Presigner에서 같이 사용)
    private StaticCredentialsProvider credentialsProvider(AwsS3Properties properties) {
        return StaticCredentialsProvider.create(
            AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey()));
    }

}
