package com.sprint.deokhugam.book.adapter.in.web;

import static com.sprint.deokhugam.common.logging.MdcLoggingInterceptor.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import com.sprint.deokhugam.book.adapter.in.web.dto.BookCreateRequest;
import com.sprint.deokhugam.book.adapter.in.web.dto.BookDto;
import com.sprint.deokhugam.book.adapter.in.web.dto.BookUpdateRequest;
import com.sprint.deokhugam.book.application.port.in.BookUseCase;
import com.sprint.deokhugam.book.application.port.in.OcrUseCase;
import com.sprint.deokhugam.book.domain.exception.BookErrorCode;
import com.sprint.deokhugam.book.domain.exception.BookException;
import com.sprint.deokhugam.common.context.user.UserContextHolder;
import com.sprint.deokhugam.common.exception.CommonErrorCode;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookUseCase bookUseCase;

    @MockitoBean
    private OcrUseCase ocrUseCase;

    private final UUID userId = UUID.randomUUID();

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    @DisplayName("유효한 도서 정보를 전달하면 도서가 등록되고 201 응답을 반환한다.")
    void createBookSuccess() throws Exception {
        // given
        UserContextHolder.setUserId(userId);

        BookCreateRequest request = new BookCreateRequest(
            "원피스 99: 밀짚모자 루피 (밀짚모자 루피)",
            "오다 에이치로",
            "간부들의 맹추격을 동료들이 저지하고, 옥상을 향한 루피는 마침내 카이도에게?! "
                + "큰 무대에 모인 배우들의 격전이 지금, 시작되려고 한다... 오니가시마 결전 최대 히트 업!! "
                + "'원피스'를 둘러싼 해양 모험 로망!!",
            "대원씨아이",
            LocalDate.of(2023, 8, 16),
            "9791168945456"
        );

        BookDto response = new BookDto(
            UUID.randomUUID(),
            request.title(),
            request.author(),
            request.description(),
            request.publisher(),
            request.publishedDate(),
            request.isbn(),
            null,
            0,
            0.0,
            Instant.now(),
            Instant.now()
        );

        given(bookUseCase.createBook(
            eq(userId),
            any(BookCreateRequest.class),
            nullable(MultipartFile.class)
        )).willReturn(response);

        String requestJson = """
            {
                "title": "원피스 99: 밀짚모자 루피 (밀짚모자 루피)",
                "author": "오다 에이치로",
                "description": "간부들의 맹추격을 동료들이 저지하고, 옥상을 향한 루피는 마침내 카이도에게?! 큰 무대에 모인 배우들의 격전이 지금, 시작되려고 한다...",
                "publisher": "대원씨아이",
                "publishedDate": "2023-08-16",
                "isbn": "9791168945456"
            }
            """;

        // when & then
        mockMvc.perform(multipart("/api/books")
                .part(createJsonPart(requestJson))
                .header(USER_ID_HEADER, userId.toString()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("원피스 99: 밀짚모자 루피 (밀짚모자 루피)"))
            .andExpect(jsonPath("$.author").value("오다 에이치로"))
            .andExpect(jsonPath("$.isbn").value("9791168945456"));

        verify(bookUseCase).createBook(
            eq(userId),
            any(BookCreateRequest.class),
            nullable(MultipartFile.class)
        );
    }

    @Test
    @DisplayName("ISBN 형식이 잘못되면 400 Bad Request를 반환한다.")
    void createBook_InvalidIsbn_ReturnsBadRequest() throws Exception {
        // given
        UserContextHolder.setUserId(userId);

        String requestJson = """
            {
                "title": "원피스 99: 밀짚모자 루피 (밀짚모자 루피)",
                "author": "오다 에이치로",
                "description": "간부들의 맹추격을 동료들이 저지하고, 옥상을 향한 루피는 마침내 카이도에게?! 큰 무대에 모인 배우들의 격전이 지금, 시작되려고 한다...",
                "publisher": "대원씨아이",
                "publishedDate": "2023-08-16",
                "isbn": "invalid-isbn"
            }
            """;

        given(bookUseCase.createBook(
            eq(userId),
            any(BookCreateRequest.class),
            nullable(MultipartFile.class)
        )).willThrow(new BookException(BookErrorCode.BOOK_INVALID_ISBN));

        // when & then
        mockMvc.perform(multipart("/api/books")
                .part(createJsonPart(requestJson))
                .header(USER_ID_HEADER, userId.toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(BookErrorCode.BOOK_INVALID_ISBN.getMessage()));
    }

    @Test
    @DisplayName("이미 등록된 ISBN이면 409 Conflict를 반환한다.")
    void createBook_DuplicateIsbn_ReturnsConflict() throws Exception {
        // given
        UserContextHolder.setUserId(userId);

        String requestJson = """
            {
                "title": "원피스 99: 밀짚모자 루피 (밀짚모자 루피)",
                "author": "오다 에이치로",
                "description": "간부들의 맹추격을 동료들이 저지하고, 옥상을 향한 루피는 마침내 카이도에게?! 큰 무대에 모인 배우들의 격전이 지금, 시작되려고 한다...",
                "publisher": "대원씨아이",
                "publishedDate": "2023-08-16",
                "isbn": "9791168945456"
            }
            """;

        given(bookUseCase.createBook(
            eq(userId),
            any(BookCreateRequest.class),
            nullable(MultipartFile.class)
        )).willThrow(new BookException(BookErrorCode.BOOK_ISBN_DUPLICATE));

        // when & then
        mockMvc.perform(multipart("/api/books")
                .part(createJsonPart(requestJson))
                .header(USER_ID_HEADER, userId.toString()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value(BookErrorCode.BOOK_ISBN_DUPLICATE.getMessage()));
    }

    @Test
    @DisplayName("잘못된 JSON 요청하면 400 Bad Request를 반환한다.")
    void createBook_InvalidJson_ReturnsBadRequest() throws Exception {
        // given
        UserContextHolder.setUserId(userId);

        String invalidJson = """
            {
                "title": "원피스 99: 밀짚모자 루피 (밀짚모자 루피)",
                "isbn":
            }
            """;

        // when & then
        mockMvc.perform(multipart("/api/books")
                .part(createJsonPart(invalidJson))
                .header(USER_ID_HEADER, userId.toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(CommonErrorCode.INVALID_INPUT.getMessage()));
    }

    @Test
    @DisplayName("도서 삭제 요청을 전달하면 204 No Content를 반환한다.")
    void deleteBookSuccess() throws Exception {
        // given
        UUID bookId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/books/{bookId}", bookId))
            .andExpect(status().isNoContent());

        then(bookUseCase).should().deleteBook(bookId);
    }

    @Test
    @DisplayName("존재하지 않는 도서를 삭제하면 404 Not Found를 반환한다.")
    void deleteBookNotFound() throws Exception {
        // given
        UUID bookId = UUID.randomUUID();

        willThrow(new BookException(BookErrorCode.BOOK_NOT_FOUND))
            .given(bookUseCase)
            .deleteBook(bookId);

        // when & then
        mockMvc.perform(delete("/api/books/{bookId}", bookId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value(
                BookErrorCode.BOOK_NOT_FOUND.getMessage()
            ));

        then(bookUseCase).should().deleteBook(bookId);
    }

    @Test
    @DisplayName("잘못된 도서 ID를 전달하면 400 Bad Request를 반환한다.")
    void deleteBookInvalidId() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/books/{bookId}", "invalid-uuid"))
            .andExpect(status().isBadRequest());

        then(bookUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("도서 상세 조회에 성공하면 200과 도서 정보를 반환한다.")
    void getBookSuccess() throws Exception {
        // given
        UUID bookId = UUID.randomUUID();
        BookDto response = new BookDto(
            bookId,
            "제목",
            "저자",
            "설명",
            "출판사",
            LocalDate.of(2023, 8, 16),
            "9791168945456",
            null,
            0,
            0.0,
            Instant.now(),
            Instant.now());
        given(bookUseCase.getBook(bookId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/books/{bookId}", bookId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(bookId.toString()))
            .andExpect(jsonPath("$.title").value("제목"));
    }

    @Test
    @DisplayName("존재하지 않는 도서를 조회하면 404를 반환한다.")
    void getBookNotFound() throws Exception {
        // given
        UUID bookId = UUID.randomUUID();
        given(bookUseCase.getBook(bookId)).willThrow(
            new BookException(BookErrorCode.BOOK_NOT_FOUND)
        );

        // when & then
        mockMvc.perform(get("/api/books/{bookId}", bookId))
            .andExpect(status().isNotFound());
    }

    private MockPart createJsonPart(String json) {
        MockPart part = new MockPart(
            "bookData",
            json.getBytes(StandardCharsets.UTF_8)
        );

        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return part;
    }

    @Test
    @DisplayName("유효한 도서 정보를 전달하면 도서가 수정되고 200 응답을 반환한다.")
    void updateBookSuccess() throws Exception {
        // given
        UUID bookId = UUID.randomUUID();

        String requestJson = """
            {
                "title": "수정된 제목",
                "author": "수정된 저자",
                "description": "수정된 설명",
                "publisher": "수정된 출판사",
                "publishedDate": "2026-11-02"
            }
            """;

        BookDto response = new BookDto(
            bookId,
            "수정된 제목",
            "수정된 저자",
            "수정된 설명",
            "수정된 출판사",
            LocalDate.of(2026, 11, 2),
            "9791168945456",
            "https://example.com/thumbnail.png",
            0,
            0.0,
            Instant.now(),
            Instant.now()
        );

        MockMultipartFile thumbnailImage = new MockMultipartFile(
            "thumbnailImage",
            "thumbnail.png",
            MediaType.IMAGE_PNG_VALUE,
            new byte[] {1, 2, 3}
        );

        given(bookUseCase.updateBook(
            eq(bookId),
            any(BookUpdateRequest.class),
            any(MultipartFile.class)
        )).willReturn(response);

        // when & then
        mockMvc.perform(multipart("/api/books/{bookId}", bookId)
                .file(thumbnailImage)
                .part(createJsonPart(requestJson))
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                }))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(bookId.toString()))
            .andExpect(jsonPath("$.title").value("수정된 제목"))
            .andExpect(jsonPath("$.author").value("수정된 저자"))
            .andExpect(jsonPath("$.publisher").value("수정된 출판사"))
            .andExpect(jsonPath("$.isbn").value("9791168945456"));

        then(bookUseCase).should().updateBook(
            eq(bookId),
            any(BookUpdateRequest.class),
            any(MultipartFile.class)
        );
    }

    @Test
    @DisplayName("썸네일 이미지 없이 도서 정보를 수정할 수 있다.")
    void updateBookWithoutThumbnail() throws Exception {
        // given
        UUID bookId = UUID.randomUUID();

        String requestJson = """
                {
                    "title": "수정된 제목",
                    "author": "수정된 저자",
                    "description": "수정된 설명",
                    "publisher": "수정된 출판사",
                    "publishedDate": "2026-11-02"
                }
            """;

        BookDto response = new BookDto(
            bookId,
            "수정된 제목",
            "수정된 저자",
            "수정된 설명",
            "수정된 출판사",
            LocalDate.of(2026, 11, 2),
            "9791168945456",
            null,
            0,
            0.0,
            Instant.now(),
            Instant.now()
        );

        given(bookUseCase.updateBook(
            eq(bookId),
            any(BookUpdateRequest.class),
            nullable(MultipartFile.class)
        )).willReturn(response);

        // when & then
        mockMvc.perform(multipart("/api/books/{bookId}", bookId)
                .part(createJsonPart(requestJson))
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                }))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("수정된 제목"))
            .andExpect(jsonPath("$.author").value("수정된 저자"));

        then(bookUseCase).should().updateBook(
            eq(bookId),
            any(BookUpdateRequest.class),
            nullable(MultipartFile.class)
        );
    }

    @Test
    @DisplayName("존재하지 않는 도서를 수정하면 404 Not Found를 반환한다.")
    void updateBookNotFound() throws Exception {
        // given
        UUID bookId = UUID.randomUUID();

        String requestJson = """
                {
                    "title": "수정된 제목",
                    "author": "수정된 저자",
                    "description": "수정된 설명",
                    "publisher": "수정된 출판사",
                    "publishedDate": "2026-11-02"
                }
            """;

        willThrow(new BookException(BookErrorCode.BOOK_NOT_FOUND))
            .given(bookUseCase)
            .updateBook(
                eq(bookId),
                any(BookUpdateRequest.class),
                nullable(MultipartFile.class)
            );

        // when & then
        mockMvc.perform(multipart("/api/books/{bookId}", bookId)
                .part(createJsonPart(requestJson))
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                }))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value(
                BookErrorCode.BOOK_NOT_FOUND.getMessage()
            ));

        then(bookUseCase).should().updateBook(
            eq(bookId),
            any(BookUpdateRequest.class),
            nullable(MultipartFile.class)
        );
    }

    @Test
    @DisplayName("수정할 정보가 없으면 400 Bad Request를 반환한다.")
    void updateBookInvalidRequest() throws Exception {
        // given
        UUID bookId = UUID.randomUUID();

        String requestJson = """
                {
                    "title": null,
                    "author": null,
                    "description": null,
                    "publisher": null,
                    "publishedDate": null
                }
            """;

        willThrow(new BookException(BookErrorCode.BOOK_INVALID_UPDATE))
            .given(bookUseCase)
            .updateBook(
                eq(bookId),
                any(BookUpdateRequest.class),
                nullable(MultipartFile.class)
            );

        // when & then
        mockMvc.perform(multipart("/api/books/{bookId}", bookId)
                .part(createJsonPart(requestJson))
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                }))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(
                BookErrorCode.BOOK_INVALID_UPDATE.getMessage()
            ));

        then(bookUseCase).should().updateBook(
            eq(bookId),
            any(BookUpdateRequest.class),
            nullable(MultipartFile.class)
        );
    }

    @Test
    @DisplayName("잘못된 도서 ID를 전달하면 400 Bad Request를 반환한다.")
    void updateBookInvalidId() throws Exception {
        // given
        String requestJson = """
                {
                    "title": "수정된 제목",
                    "author": "수정된 저자",
                    "description": "수정된 설명",
                    "publisher": "수정된 출판사",
                    "publishedDate": "2026-11-02"
                }
            """;

        // when & then
        mockMvc.perform(multipart("/api/books/{bookId}", "invalid-uuid")
                .part(createJsonPart(requestJson))
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                }))
            .andExpect(status().isBadRequest());

        then(bookUseCase).shouldHaveNoInteractions();
    }
}
