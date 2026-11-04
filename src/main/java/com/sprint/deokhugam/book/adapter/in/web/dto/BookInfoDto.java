package com.sprint.deokhugam.book.adapter.in.web.dto;

import java.time.LocalDate;

public record BookInfoDto(
    String title,
    String author,
    String description,
    String publisher,
    LocalDate publishedDate,
    String isbn,
    byte[] thumbnailImage
) {
}
