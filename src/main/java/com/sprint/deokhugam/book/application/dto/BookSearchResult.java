package com.sprint.deokhugam.book.application.dto;

import java.time.LocalDate;
import java.util.List;

// 외부 카카오 API 응답
public record BookSearchResult(
    String title,
    String contents,
    String url,
    String isbn,
    LocalDate publishedDate,
    List<String> authors,
    String publisher,
    List<String> translators,
    Integer price,
    Integer salePrice,
    String thumbnail,
    String status
) {
}
