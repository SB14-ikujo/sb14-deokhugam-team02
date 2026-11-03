package com.sprint.deokhugam.book.adapter.out.external;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Base64;

import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.book.application.port.out.OcrClient;
import com.sprint.deokhugam.book.domain.exception.BookErrorCode;
import com.sprint.deokhugam.book.domain.exception.BookException;
import com.sprint.deokhugam.common.config.OcrSpaceProperties;

@Slf4j
@Component
public class OcrSpaceClient implements OcrClient {
    private static final String DEFAULT_CONTENT_TYPE = "image/jpeg";

    private final RestClient restClient;
    private final OcrSpaceProperties properties;

    public OcrSpaceClient(OcrSpaceProperties properties) {
        this.properties = properties;

        Duration timeout = Duration.ofSeconds(properties.getTimeoutSeconds());
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(timeout)
            .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(timeout);

        this.restClient = RestClient.builder()
            .requestFactory(requestFactory)
            .build();
    }

    @Override
    public String readText(byte[] image, String contentType) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            log.error("OCR Space API 키가 설정되지 않았습니다. .env.local(운영은 .env.prod)의 OCR_SPACE_API_KEY를 확인하세요.");
            throw new BookException(BookErrorCode.BOOK_OCR_FAILED);
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("base64Image", toDataUri(image, contentType));
        form.add("language", "eng");
        form.add("OCREngine", "2");  // 2번 엔진이 숫자·특수문자 인식에 더 강함
        form.add("scale", "true");   // 작은 글씨를 키워서 인식

        OcrSpaceResponse response;
        try {
            response = restClient.post()
                .uri(properties.getUrl())
                .header("apikey", properties.getApiKey())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(form)
                .retrieve()
                .body(OcrSpaceResponse.class);
        } catch (RestClientException ex) {
            log.error("OCR Space API 호출 실패", ex);
            throw new BookException(BookErrorCode.BOOK_OCR_FAILED);
        }

        if (response == null || response.erroredOnProcessing()
            || response.parsedResults() == null || response.parsedResults().isEmpty()) {
            log.warn("OCR Space 처리 실패: errorMessage={}", response == null ? null : response.errorMessage());
            throw new BookException(BookErrorCode.BOOK_OCR_FAILED);
        }

        String text = response.parsedResults().get(0).parsedText();
        log.debug("OCR 인식 결과: {}", text);

        return text == null ? "" : text;
    }

    // OCR Space가 받는 형식: data:image/jpeg;base64,/9j/4AAQ...
    private String toDataUri(byte[] image, String contentType) {
        String mediaType = (contentType == null || contentType.isBlank()) ? DEFAULT_CONTENT_TYPE : contentType;

        return "data:" + mediaType + ";base64," + Base64.getEncoder().encodeToString(image);
    }
}
