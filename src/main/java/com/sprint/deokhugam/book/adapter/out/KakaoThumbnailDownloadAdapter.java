package com.sprint.deokhugam.book.adapter.out;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.book.application.dto.DownloadedThumbnail;
import com.sprint.deokhugam.book.application.port.out.BookThumbnailDownloadPort;

@Slf4j
@Component
public class KakaoThumbnailDownloadAdapter implements BookThumbnailDownloadPort {
    private static final int MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final int BUFFER_SIZE = 8192;

    private static final MediaType IMAGE_WEBP = MediaType.parseMediaType("image/webp");

    private final RestClient restClient = RestClient.create();

    @Override
    public Optional<DownloadedThumbnail> download(String thumbnailUrl) {
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            return Optional.empty();
        }

        try {
            URI uri = URI.create(thumbnailUrl);

            if (!isAllowedHost(uri)) {
                log.warn("허용되지 않은 썸네일 URL");

                return Optional.empty();
            }

            return restClient.get()
                .uri(uri)
                .exchange((request, response) -> {
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        log.warn("카카오 썸네일 다운로드 응답 실패 : {}", response.getStatusCode());

                        return Optional.empty();
                    }

                    // 1. Content-Length 사전 검사
                    long contentLength = response.getHeaders().getContentLength();

                    // Content-Length가 존재하고 5MB를 초과하면 본문을 읽고 중단
                    if (contentLength > MAX_IMAGE_SIZE) {
                        log.warn("카카오 썸네일 크기 초과 : {}", contentLength);

                        return Optional.empty();
                    }

                    MediaType mediaType = response.getHeaders().getContentType();

                    if (mediaType == null || !isSupportedImageType(mediaType)) {
                        log.warn("지원하지 않는 카카오 썸네일 형식");

                        return Optional.empty();
                    }

                    // 실제 스트리밍 데이터 크기를 검사
                    byte[] bytes = readLimited(response.getBody());

                    if (bytes.length == 0) {
                        log.warn("카카오 썸네일 응답 본문이 비어 있음");

                        return Optional.empty();
                    }

                    String contentType = mediaType.toString();
                    String filename = "kakao-thumbnail." + getExtenstion(mediaType);

                    return Optional.of(
                        new DownloadedThumbnail(bytes, contentType, filename)
                    );
                });
        } catch (IllegalArgumentException | RestClientException e) {
            log.warn("카카오 썸네일 다운로드 실패");

            return Optional.empty();
        }
    }

    private boolean isAllowedHost(URI uri) {
        String host = uri.getHost();

        return "https".equalsIgnoreCase(uri.getScheme())
            && host != null
            && (host.equals("kakaocdn.net") || host.endsWith(".kakaocdn.net"));
    }

    private byte[] readLimited(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int totalBytes = 0;
            int bytesRead;

            // 실제 데이터를 8KB 단위로 읽으면 누적 크기 확인 후 만약 5MB 초과하는 순간 예외 발생
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                totalBytes += bytesRead;

                if (totalBytes > MAX_IMAGE_SIZE) {
                    throw new IllegalArgumentException("썸네일 이미지 크기는 5MB를 초과할 수 없습니다.");
                }

                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
        }
    }

    private boolean isSupportedImageType(MediaType mediaType) {
        return MediaType.IMAGE_JPEG.isCompatibleWith(mediaType)
            || MediaType.IMAGE_PNG.isCompatibleWith(mediaType)
            || IMAGE_WEBP.isCompatibleWith(mediaType)
            || MediaType.IMAGE_GIF.isCompatibleWith(mediaType);
    }

    private String getExtenstion(MediaType mediaType) {
        if (MediaType.IMAGE_JPEG.isCompatibleWith(mediaType)) {
            return "jpg";
        }

        if (MediaType.IMAGE_PNG.isCompatibleWith(mediaType)) {
            return "png";
        }

        if (MediaType.IMAGE_GIF.isCompatibleWith(mediaType)) {
            return "gif";
        }

        return "webp";
    }
}
