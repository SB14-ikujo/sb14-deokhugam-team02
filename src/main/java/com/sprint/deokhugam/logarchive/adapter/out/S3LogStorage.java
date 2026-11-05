package com.sprint.deokhugam.logarchive.adapter.out;

import java.nio.file.Path;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import com.sprint.deokhugam.common.config.AwsS3Properties;
import com.sprint.deokhugam.logarchive.application.out.LogStoragePort;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "deokhugam.storage.type", havingValue = "s3")
public class S3LogStorage implements LogStoragePort {

    private final S3Client s3Client;
    private final AwsS3Properties properties;

    @Override
    public void upload(String fileName, Path file) {
        String key = "logs/" + fileName;

        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(key)
                .contentType("text/plain")
                .build(),
            RequestBody.fromFile(file) // 파일에서 바로 스트리밍으로
        );
    }
}
