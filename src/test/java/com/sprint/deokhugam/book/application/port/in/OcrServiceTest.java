package com.sprint.deokhugam.book.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sprint.deokhugam.book.application.port.out.OcrClient;
import com.sprint.deokhugam.book.domain.exception.BookErrorCode;
import com.sprint.deokhugam.book.domain.exception.BookException;

@ExtendWith(MockitoExtension.class)
class OcrServiceTest {
    private static final byte[] IMAGE = {1, 2, 3};
    private static final String CONTENT_TYPE = "image/png";

    @Mock
    private OcrClient ocrClient;

    @InjectMocks
    private OcrService ocrService;

    @ParameterizedTest(name = "[{index}] {0}")
    @CsvSource(delimiter = '|', value = {
        "ISBN 978-89-374-6044-9 03810 | 9788937460449",
        "ISBN 978 89 374 6044 9       | 9788937460449",
        "9788937460449                | 9788937460449",
        "ISBN 979-11-6689-185-4       | 9791166891854"
    })
    @DisplayName("OCR 결과에 하이픈이나 공백이 섞인 ISBN-13이 있으면 숫자만 남겨 반환한다.")
    void extractIsbn_Isbn13(String text, String expected) {
        // given
        given(ocrClient.readText(IMAGE, CONTENT_TYPE)).willReturn(text);

        // when
        String isbn = ocrService.extractIsbn(IMAGE, CONTENT_TYPE);

        // then
        assertThat(isbn).isEqualTo(expected);
    }

    @Test
    @DisplayName("가격이나 체크 숫자가 틀린 번호는 건너뛰고 유효한 ISBN을 반환한다.")
    void extractIsbn_SkipsInvalidCandidates() {
        // given
        String text = """
            값 9,900원
            9788937460440
            ISBN 978-89-374-6044-9
            """;
        given(ocrClient.readText(IMAGE, CONTENT_TYPE)).willReturn(text);

        // when
        String isbn = ocrService.extractIsbn(IMAGE, CONTENT_TYPE);

        // then
        assertThat(isbn).isEqualTo("9788937460449");
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @CsvSource(delimiter = '|', value = {
        "ISBN 89-374-6044-0 | 8937460440",
        "ISBN 0-8044-2957-x | 080442957X"
    })
    @DisplayName("ISBN-13이 없으면 ISBN-10을 찾아 반환한다.")
    void extractIsbn_Isbn10(String text, String expected) {
        // given
        given(ocrClient.readText(IMAGE, CONTENT_TYPE)).willReturn(text);

        // when
        String isbn = ocrService.extractIsbn(IMAGE, CONTENT_TYPE);

        // then
        assertThat(isbn).isEqualTo(expected);
    }

    @Test
    @DisplayName("ISBN-13과 ISBN-10이 함께 있으면 ISBN-13을 우선 반환한다.")
    void extractIsbn_PrefersIsbn13() {
        // given
        given(ocrClient.readText(IMAGE, CONTENT_TYPE))
            .willReturn("ISBN 89-374-6044-0\nISBN 978-89-374-6044-9");

        // when
        String isbn = ocrService.extractIsbn(IMAGE, CONTENT_TYPE);

        // then
        assertThat(isbn).isEqualTo("9788937460449");
    }

    @Test
    @DisplayName("OCR 결과에 유효한 ISBN이 없으면 BOOK_OCR_FAILED 예외가 발생한다.")
    void extractIsbn_NoIsbn_ThrowsException() {
        // given
        given(ocrClient.readText(IMAGE, CONTENT_TYPE)).willReturn("침팬지 폴리틱스\n값 9,900원");

        // when & then
        assertThatThrownBy(() -> ocrService.extractIsbn(IMAGE, CONTENT_TYPE))
            .isInstanceOf(BookException.class)
            .extracting("errorCode")
            .isEqualTo(BookErrorCode.BOOK_OCR_FAILED);
    }

    @Test
    @DisplayName("빈 이미지면 OCR을 호출하지 않고 BOOK_OCR_FAILED 예외가 발생한다.")
    void extractIsbn_EmptyImage_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> ocrService.extractIsbn(new byte[0], CONTENT_TYPE))
            .isInstanceOf(BookException.class)
            .extracting("errorCode")
            .isEqualTo(BookErrorCode.BOOK_OCR_FAILED);

        then(ocrClient).should(never()).readText(any(), any());
    }

    @Test
    @DisplayName("OCR 호출이 실패하면 예외를 그대로 전달한다.")
    void extractIsbn_OcrClientFails_ThrowsException() {
        // given
        given(ocrClient.readText(IMAGE, CONTENT_TYPE))
            .willThrow(new BookException(BookErrorCode.BOOK_OCR_FAILED));

        // when & then
        assertThatThrownBy(() -> ocrService.extractIsbn(IMAGE, CONTENT_TYPE))
            .isInstanceOf(BookException.class)
            .extracting("errorCode")
            .isEqualTo(BookErrorCode.BOOK_OCR_FAILED);
    }
}
