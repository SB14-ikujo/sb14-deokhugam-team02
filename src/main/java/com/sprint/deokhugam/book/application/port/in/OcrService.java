package com.sprint.deokhugam.book.application.port.in;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.book.application.port.out.OcrClient;
import com.sprint.deokhugam.book.domain.exception.BookErrorCode;
import com.sprint.deokhugam.book.domain.exception.BookException;
import com.sprint.deokhugam.book.domain.validator.IsbnValidator;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrService implements OcrUseCase {
    private static final Pattern ISBN13_PATTERN = Pattern.compile("(?<!\\d)97[89](?:[\\s-]?\\d){10}(?!\\d)");

    // ISBN-10: 숫자 9자리 + 마지막 자리는 숫자 또는 X
    private static final Pattern ISBN10_PATTERN = Pattern.compile("(?<!\\d)\\d(?:[\\s-]?\\d){8}[\\s-]?[\\dXx](?!\\d)");

    private final OcrClient ocrClient;

    @Override
    public String extractIsbn(byte[] image, String contentType) {
        if (image == null || image.length == 0) {
            throw new BookException(BookErrorCode.BOOK_OCR_FAILED);
        }

        String text = ocrClient.readText(image, contentType);

        String isbn = findValidIsbn(ISBN13_PATTERN, text);
        if (isbn == null) {
            isbn = findValidIsbn(ISBN10_PATTERN, text);
        }

        if (isbn == null) {
            log.warn("OCR 결과에서 ISBN을 찾지 못했습니다. text={}", text);
            throw new BookException(BookErrorCode.BOOK_OCR_FAILED);
        }

        return isbn;
    }

    // 패턴에 맞는 후보 중 체크 숫자까지 맞는 첫 번째 값을 반환
    private String findValidIsbn(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String candidate = matcher.group()
                .replaceAll("[\\s-]", "")
                .toUpperCase(Locale.ROOT);

            if (IsbnValidator.isValidIsbn13(candidate) || IsbnValidator.isValidIsbn10(candidate)) {
                return candidate;
            }
        }

        return null;
    }
}
