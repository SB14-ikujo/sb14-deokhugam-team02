package com.sprint.deokhugam.book.adapter.out;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.sprint.deokhugam.book.application.dto.DownloadedThumbnail;

class KakaoThumbnailDownloadAdapterTest {
    private static final String THUMBNAIL_URL =
        "https://search1.kakaocdn.net/thumbnail/test.png";

    private static final int MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    private MockRestServiceServer mockServer;

    private KakaoThumbnailDownloadAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();

        mockServer = MockRestServiceServer.bindTo(builder).build();

        adapter = new KakaoThumbnailDownloadAdapter(builder.build());
    }

    @AfterEach
    void tearDown() {
        mockServer.verify();
    }

    @Test
    @DisplayName("허용된 카카오 CDN URL의 썸네일 다운로드에 성공한다")
    void downloadSuccess() {
        // given
        byte[] imageBytes = {1, 2, 3};

        mockServer.expect(requestTo(THUMBNAIL_URL))
            .andRespond(withSuccess(imageBytes, MediaType.IMAGE_PNG));

        // when
        Optional<DownloadedThumbnail> result = adapter.download(THUMBNAIL_URL);

        // then
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("썸네일 URL이 null이면 빈 Optional을 반환한다")
    void downloadWithNullUrl() {
        // when
        Optional<DownloadedThumbnail> result = adapter.download(null);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("썸네일 URL이 빈 문자열이면 빈 Optional을 반환한다")
    void downloadWithBlankUrl() {
        // when
        Optional<DownloadedThumbnail> result = adapter.download(" ");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("허용되지 않은 호스트의 URL이면 다운로드하지 않는다")
    void downloadWithDisallowedHost() {
        // given
        String invalidUrl = "https://example.com/thumbnail.png";

        // when
        Optional<DownloadedThumbnail> result = adapter.download(invalidUrl);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("HTTPS가 아닌 URL이면 다운로드하지 않는다")
    void downloadWithHttpUrl() {
        // given
        String invalidUrl = "http://search1.kakaocdn.net/thumbnail/test.png";

        // when
        Optional<DownloadedThumbnail> result = adapter.download(invalidUrl);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("HTTP 응답 상태 코드가 2xx가 아니면 빈 Optional을 반환한다")
    void downloadWithNonSuccessStatus() {
        // given
        mockServer.expect(requestTo(THUMBNAIL_URL))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // when
        Optional<DownloadedThumbnail> result = adapter.download(THUMBNAIL_URL);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("지원하지 않는 이미지 형식이면 빈 Optional을 반환한다")
    void downloadWithUnsupportedImageType() {
        // given
        mockServer.expect(requestTo(THUMBNAIL_URL))
            .andRespond(withSuccess("not an image", MediaType.TEXT_PLAIN));

        // when
        Optional<DownloadedThumbnail> result = adapter.download(THUMBNAIL_URL);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("응답 본문이 비어 있으면 빈 Optional을 반환한다")
    void downloadWithEmptyBody() {
        // given
        mockServer.expect(requestTo(THUMBNAIL_URL))
            .andRespond(withSuccess(new byte[0], MediaType.IMAGE_PNG));

        // when
        Optional<DownloadedThumbnail> result = adapter.download(THUMBNAIL_URL);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("썸네일 크기가 5MB를 초과하면 빈 Optional을 반환한다")
    void downloadWithOversizedImage() {
        // given
        byte[] oversizedImage = new byte[MAX_IMAGE_SIZE + 1];

        mockServer.expect(requestTo(THUMBNAIL_URL))
            .andRespond(withSuccess(oversizedImage, MediaType.IMAGE_PNG));

        // when
        Optional<DownloadedThumbnail> result = adapter.download(THUMBNAIL_URL);

        // then
        assertThat(result).isEmpty();
    }
}
