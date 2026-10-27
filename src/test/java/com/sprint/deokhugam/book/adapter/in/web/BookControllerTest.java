package com.sprint.deokhugam.book.adapter.in.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sprint.deokhugam.book.adapter.in.web.dto.BookCreateRequest;
import com.sprint.deokhugam.book.adapter.in.web.dto.BookDto;
import com.sprint.deokhugam.book.application.port.in.BookUseCase;
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

        given(bookUseCase.createBook(eq(userId), any(BookCreateRequest.class)))
            .willReturn(response);

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
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("원피스 99: 밀짚모자 루피 (밀짚모자 루피)"))
            .andExpect(jsonPath("$.author").value("오다 에이치로"))
            .andExpect(jsonPath("$.isbn").value("9791168945456"));

        verify(bookUseCase).createBook(eq(userId), any(BookCreateRequest.class));
    }

    @Test
    @DisplayName("ISBN 형식이 잘못되면 400 Bad Request를 반환한다.")
    void createBook_InvalidIsbn_ReturnsBadRequest() throws Exception {
        // given
        UserContextHolder.setUserId(userId);

        String responseBody = """
            {
                "title": "원피스 99: 밀짚모자 루피 (밀짚모자 루피)",
                "author": "오다 에이치로",
                "description": "간부들의 맹추격을 동료들이 저지하고, 옥상을 향한 루피는 마침내 카이도에게?! 큰 무대에 모인 배우들의 격전이 지금, 시작되려고 한다...",
                "publisher": "대원씨아이",
                "publishedDate": "2023-08-16",
                "isbn": "invalid-isbn"
            }
            """;

        given(bookUseCase.createBook(eq(userId), any(BookCreateRequest.class)))
            .willThrow(new BookException(BookErrorCode.BOOK_INVALID_ISBN));

        // when & then
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(responseBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(BookErrorCode.BOOK_INVALID_ISBN.getMessage()));
    }

    @Test
    @DisplayName("이미 등록된 ISBN이면 409 Conflict를 반환한다.")
    void createBook_DuplicateIsbn_ReturnsConflict() throws Exception {
        // given
        UserContextHolder.setUserId(userId);

        String responseBody = """
            {
                "title": "원피스 99: 밀짚모자 루피 (밀짚모자 루피)",
                "author": "오다 에이치로",
                "description": "간부들의 맹추격을 동료들이 저지하고, 옥상을 향한 루피는 마침내 카이도에게?! 큰 무대에 모인 배우들의 격전이 지금, 시작되려고 한다...",
                "publisher": "대원씨아이",
                "publishedDate": "2023-08-16",
                "isbn": "9791168945456"
            }
            """;

        given(bookUseCase.createBook(eq(userId), any(BookCreateRequest.class)))
            .willThrow(new BookException(BookErrorCode.BOOK_ISBN_DUPLICATE));

        // when & then
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(responseBody))
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
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(CommonErrorCode.INVALID_INPUT.getMessage()));
    }
}
