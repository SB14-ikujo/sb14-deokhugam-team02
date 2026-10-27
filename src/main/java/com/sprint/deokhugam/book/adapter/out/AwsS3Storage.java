package com.sprint.deokhugam.book.adapter.out;

import java.time.Duration;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import com.sprint.deokhugam.book.adapter.out.exception.StorageErrorCode;
import com.sprint.deokhugam.book.adapter.out.exception.StorageException;
import com.sprint.deokhugam.book.application.port.out.BookImageStorage;
import com.sprint.deokhugam.common.config.AwsS3Properties;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "deokhugam.storage.type", havingValue = "s3")
public class AwsS3Storage implements BookImageStorage {
    private final AwsS3Properties awsS3Properties;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Override
    public String generateImageKey(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        return uuid + "_" + originalFilename;
    }

    @Override
    public String upload(String imageKey, byte[] value, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(awsS3Properties.getBucket())
            .key(imageKey)
            .contentType(contentType)
            .build();
        try {
            s3Client.putObject(request, RequestBody.fromBytes(value));
            return imageKey;
        } catch (AwsServiceException e) {
            throw new StorageException(StorageErrorCode.S3_UPLOAD_FAILED, e);
        } catch (SdkClientException e) {
            throw new StorageException(StorageErrorCode.S3_CONNECTION_FAILED, e);
        }
    }

    @Override
    public String getImageUrl(String imageKey, String contentType) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(awsS3Properties.getBucket())
            .key(imageKey)
            .responseContentType(contentType)
            .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(awsS3Properties.getPresignedUrlExpiration()))
            .getObjectRequest(getObjectRequest)
            .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

        log.info("이미지 Presigned URL 생성 완료: [{}]", presignedRequest.url());
        log.info("이미지 요청 HTTP 메서드: [{}]", presignedRequest.httpRequest().method());

        return presignedRequest.url().toString();
    }

    @Override
    public void delete(String imageKey) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
            .bucket(awsS3Properties.getBucket())
            .key(imageKey)
            .build();

        try {
            s3Client.deleteObject(request);
        } catch (AwsServiceException e) {
            throw new StorageException(StorageErrorCode.S3_DELETE_FAILED, e);
        } catch (SdkClientException e) {
            throw new StorageException(StorageErrorCode.S3_CONNECTION_FAILED, e);
        }

    }
}
