package com.sprint.deokhugam.book.domain.validator;

import java.util.Locale;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import com.sprint.deokhugam.book.domain.exception.BookErrorCode;
import com.sprint.deokhugam.book.domain.exception.BookException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IsbnValidator {

    public static String normalizeAndValidate(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new BookException(BookErrorCode.BOOK_INVALID_ISBN);
        }

        // 하이픈과 공백 제거
        String normalizeIsbn = isbn
            .replace("-", "")
            .replaceAll("\\s", "")
            .toUpperCase(Locale.ROOT);

        if (!isValidIsbn10(normalizeIsbn) && !isValidIsbn13(normalizeIsbn)) {
            throw new BookException(BookErrorCode.BOOK_INVALID_ISBN);
        }

        return normalizeIsbn;
    }

    /*
     * ISBN-10 검증 방식
        - 앞의 9자리 각 숫자에 10부터 2까지의 가중치를 곱해 합산
        - 마지막 자리가 X인 경우 10으로 계산
        - 전체 합을 11로 나누어 나머지가 0이면 유효
     */
    public static boolean isValidIsbn10(String isbn) {
        if (!isbn.matches("\\d{9}[\\dX]")) {
            return false;
        }

        int sum = 0;

        for (int i = 0; i < 10; i++) {
            int digit = isbn.charAt(i) == 'X' ? 10 : Character.getNumericValue(isbn.charAt(i));

            sum += digit * (10 - i);
        }

        return sum % 11 == 0;
    }

    /*
     * ISBN-13 검증 방식
        - 앞의 12자리 숫자에 대해 짝수번째는 1, 홀수번째는 3의 가중치를 곱해 모두 더함
        - 10 - (합 % 10)을 계산한 값이 마지막 13번째 숫자와 일치하면 유효 (결과가 10이면 0으로 처리)
     */
    public static boolean isValidIsbn13(String isbn) {
        if (!isbn.matches("\\d{13}")) {
            return false;
        }

        int sum = 0;

        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(isbn.charAt(i));

            sum += digit * (i % 2 == 0 ? 1 : 3);
        }

        int checkDigit = (10 - sum % 10) % 10;

        return checkDigit == Character.getNumericValue(isbn.charAt(12));
    }
}
