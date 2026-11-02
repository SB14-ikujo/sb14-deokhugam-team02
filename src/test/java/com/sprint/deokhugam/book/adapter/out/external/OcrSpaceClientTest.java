package com.sprint.deokhugam.book.adapter.out.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import com.sprint.deokhugam.book.domain.exception.BookErrorCode;
import com.sprint.deokhugam.book.domain.exception.BookException;
import com.sprint.deokhugam.common.config.OcrSpaceProperties;

// 실제 OCR Space 대신 로컬 가짜 서버를 띄워서 요청·응답 처리를 검증한다
class OcrSpaceClientTest {
    private static final String PATH = "/parse/image";
    private static final byte[] IMAGE = {1, 2, 3};

    private final AtomicReference<String> receivedApiKey = new AtomicReference<>();
    private final AtomicReference<String> receivedBody = new AtomicReference<>();

    private HttpServer server;
    private OcrSpaceProperties properties;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.start();

        properties = new OcrSpaceProperties();
        properties.setApiKey("test-api-key");
        properties.setUrl("http://localhost:" + server.getAddress().getPort() + PATH);
        properties.setTimeoutSeconds(2);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    @DisplayName("OCR 성공 응답이면 인식된 텍스트를 반환하고, API 키와 이미지를 함께 보낸다.")
    void readText_Success() {
        // given
        respond(200, """
            {
                "ParsedResults": [
                    { "ParsedText": "ISBN 978-89-374-6044-9", "FileParseExitCode": 1 }
                ],
                "OCRExitCode": 1,
                "IsErroredOnProcessing": false,
                "ProcessingTimeInMilliseconds": "500"
            }
            """);
        OcrSpaceClient client = new OcrSpaceClient(properties);

        // when
        String text = client.readText(IMAGE, "image/png");

        // then
        assertThat(text).isEqualTo("ISBN 978-89-374-6044-9");
        assertThat(receivedApiKey.get()).isEqualTo("test-api-key");
        assertThat(receivedBody.get())
            .contains("name=\"base64Image\"")
            .contains("data:image/png;base64," + Base64.getEncoder().encodeToString(IMAGE))
            .contains("name=\"OCREngine\"");
    }

    @Test
    @DisplayName("Content-Type이 없으면 image/jpeg로 보낸다.")
    void readText_NoContentType_UsesJpeg() {
        // given
        respond(200, """
            { "ParsedResults": [ { "ParsedText": "" } ], "IsErroredOnProcessing": false }
            """);
        OcrSpaceClient client = new OcrSpaceClient(properties);

        // when
        client.readText(IMAGE, null);

        // then
        assertThat(receivedBody.get()).contains("data:image/jpeg;base64,");
    }

    @Test
    @DisplayName("OCR Space가 처리 실패를 응답하면 BOOK_OCR_FAILED 예외가 발생한다.")
    void readText_ErroredOnProcessing_ThrowsException() {
        // given
        respond(200, """
            {
                "OCRExitCode": 3,
                "IsErroredOnProcessing": true,
                "ErrorMessage": ["File size exceeds the maximum permissible file size limit of 1024 KB"]
            }
            """);
        OcrSpaceClient client = new OcrSpaceClient(properties);

        // when & then
        assertOcrFailed(client);
    }

    @Test
    @DisplayName("OCR Space가 4xx/5xx로 응답하면 BOOK_OCR_FAILED 예외가 발생한다.")
    void readText_HttpError_ThrowsException() {
        // given
        respond(400, """
            { "error": "E501: Not an image or PDF" }
            """);
        OcrSpaceClient client = new OcrSpaceClient(properties);

        // when & then
        assertOcrFailed(client);
    }

    @Test
    @DisplayName("응답이 타임아웃보다 늦으면 BOOK_OCR_FAILED 예외가 발생한다.")
    void readText_Timeout_ThrowsException() {
        // given
        properties.setTimeoutSeconds(1);
        server.createContext(PATH, exchange -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            exchange.close();
        });
        OcrSpaceClient client = new OcrSpaceClient(properties);

        // when & then
        assertOcrFailed(client);
    }

    @Test
    @DisplayName("API 키가 비어 있으면 OCR Space를 호출하지 않고 BOOK_OCR_FAILED 예외가 발생한다.")
    void readText_BlankApiKey_ThrowsException() {
        // given
        properties.setApiKey("");
        respond(200, """
            { "ParsedResults": [ { "ParsedText": "ISBN 978-89-374-6044-9" } ] }
            """);
        OcrSpaceClient client = new OcrSpaceClient(properties);

        // when & then
        assertOcrFailed(client);
        assertThat(receivedBody.get()).isNull();
    }

    private void respond(int status, String body) {
        server.createContext(PATH, exchange -> {
            receivedApiKey.set(exchange.getRequestHeaders().getFirst("apikey"));
            receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));

            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
    }

    private void assertOcrFailed(OcrSpaceClient client) {
        assertThatThrownBy(() -> client.readText(IMAGE, "image/png"))
            .isInstanceOf(BookException.class)
            .extracting("errorCode")
            .isEqualTo(BookErrorCode.BOOK_OCR_FAILED);
    }
}
