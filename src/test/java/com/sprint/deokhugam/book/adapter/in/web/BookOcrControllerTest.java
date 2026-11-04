package com.sprint.deokhugam.book.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sprint.deokhugam.book.application.port.in.BookUseCase;
import com.sprint.deokhugam.book.application.port.in.OcrUseCase;
import com.sprint.deokhugam.book.domain.exception.BookErrorCode;
import com.sprint.deokhugam.book.domain.exception.BookException;

@WebMvcTest(BookController.class)
class BookOcrControllerTest {
    private static final String URL = "/api/books/isbn/ocr";
    private static final byte[] IMAGE = {1, 2, 3};

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookUseCase bookUseCase;

    @MockitoBean
    private OcrUseCase ocrUseCase;

    @Test
    @DisplayName("이미지를 보내면 인식된 ISBN 문자열과 200 응답을 반환한다.")
    void extractIsbn_Success() throws Exception {
        // given
        MockMultipartFile image = new MockMultipartFile("image", "book.png", "image/png", IMAGE);
        given(ocrUseCase.extractIsbn(any(byte[].class), eq("image/png"))).willReturn("9788937460449");

        // when & then
        mockMvc.perform(multipart(URL).file(image))
            .andExpect(status().isOk())
            .andExpect(content().string("9788937460449"));

        then(ocrUseCase).should().extractIsbn(IMAGE, "image/png");
    }

    @Test
    @DisplayName("ISBN을 인식하지 못하면 400 Bad Request를 반환한다.")
    void extractIsbn_OcrFailed_ReturnsBadRequest() throws Exception {
        // given
        MockMultipartFile image = new MockMultipartFile("image", "book.png", "image/png", IMAGE);
        given(ocrUseCase.extractIsbn(any(byte[].class), any()))
            .willThrow(new BookException(BookErrorCode.BOOK_OCR_FAILED));

        // when & then
        mockMvc.perform(multipart(URL).file(image))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(BookErrorCode.BOOK_OCR_FAILED.getMessage()));
    }
}
