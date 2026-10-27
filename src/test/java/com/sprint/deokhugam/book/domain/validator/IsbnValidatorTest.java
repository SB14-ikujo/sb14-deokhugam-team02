package com.sprint.deokhugam.book.domain.validator;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.sprint.deokhugam.book.domain.exception.BookException;

class IsbnValidatorTest {
    @Nested
    @DisplayName("ISBN 정규화 및 검증")
    class NormalizeAndValidate {
        @Test
        @DisplayName("유효한 ISBN-10을 검증한다.")
        void validIsbn10() {
            String result = IsbnValidator.normalizeAndValidate("0306406152");

            assertThat(result).isEqualTo("0306406152");
        }

        @Test
        @DisplayName("유효한 ISBN-13을 검증한다.")
        void validIsbn13() {
            String result = IsbnValidator.normalizeAndValidate("9780306406157");

            assertThat(result).isEqualTo("9780306406157");
        }

        @Test
        @DisplayName("하이픈과 공백을 제거한다.")
        void normalizeIsbn() {
            String result = IsbnValidator.normalizeAndValidate("978-0-306-40615-7 ");

            assertThat(result).isEqualTo("9780306406157");
        }

        @Test
        @DisplayName("체크섬이 잘못된 ISBN은 예외가 발생한다.")
        void invalidChecksum() {
            assertThatThrownBy(() -> IsbnValidator.normalizeAndValidate("9780306406158"))
                .isInstanceOf(BookException.class);
        }

        @Test
        @DisplayName("길이가 잘못된 ISBN은 예외가 발생한다.")
        void invalidLength() {
            assertThatThrownBy(() -> IsbnValidator.normalizeAndValidate("12345"))
                .isInstanceOf(BookException.class);
        }

        @Test
        @DisplayName("ISBN이 null이면 예외가 발생한다.")
        void nullIsbn() {
            assertThatThrownBy(() -> IsbnValidator.normalizeAndValidate(null))
                .isInstanceOf(BookException.class);
        }

        @Test
        @DisplayName("ISBN이 빈 문자열이면 예외가 발생한다.")
        void blankIsbn() {
            assertThatThrownBy(() -> IsbnValidator.normalizeAndValidate(" "))
                .isInstanceOf(BookException.class);
        }
    }
}
