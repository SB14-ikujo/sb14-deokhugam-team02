package com.sprint.deokhugam.book.adapter.out;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.book.adapter.out.exception.StorageErrorCode;
import com.sprint.deokhugam.book.adapter.out.exception.StorageException;
import com.sprint.deokhugam.book.application.port.out.BookImageStorage;

@Slf4j
@Component
@ConditionalOnProperty(name = "deokhugam.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorage implements BookImageStorage {
    private static final String URL_PATH = "/api/images"; // 로컬 이미지 URL prefix

    private final Path uploadDir;

    public LocalStorage(@Value("${deokhugam.storage.path:./uploads}") String path) {
        this.uploadDir = Path.of(path).toAbsolutePath().normalize(); // 실행 위치 기준 절대 경로로 변환 불필요 경로 제거
        init();
    }

    private void init() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new StorageException(StorageErrorCode.LOCAL_UPLOAD_FAILED, e);
        }
    }

    @Override
    public String generateImageKey(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String name = Path.of(originalFilename.replace('\\', '/')).getFileName().toString();
        String safeName = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        return uuid + "_" + safeName;
    }

    @Override
    public String upload(String imageKey, byte[] value, String contentType) {
        try {
            Files.write(resolve(imageKey), value);
            return imageKey;
        } catch (IOException e) {
            throw new StorageException(StorageErrorCode.LOCAL_UPLOAD_FAILED, e);
        }
    }

    // 로컬에는 만료 개념이 없어 contentType은 사용되지 않음
    @Override
    public String getImageUrl(String imageKey, String contentType) {
        return URL_PATH + "/" + UriUtils.encodePathSegment(imageKey, "UTF-8");
    }

    @Override
    public void delete(String imageKey) {
        try {
            Files.deleteIfExists(resolve(imageKey));
        } catch (IOException e) {
            throw new StorageException(StorageErrorCode.LOCAL_DELETE_FAILED, e);
        }
    }

    // 저장 디렉터리 밖으로 벗어나는 경로(../ 등) 차단
    private Path resolve(String imageKey) {
        Path target = uploadDir.resolve(imageKey).normalize();
        if (!target.startsWith(uploadDir)) {
            throw new StorageException(StorageErrorCode.LOCAL_UPLOAD_FAILED,
                new IllegalArgumentException("잘못된 이미지 경로: " + imageKey));
        }
        return target;
    }
}
