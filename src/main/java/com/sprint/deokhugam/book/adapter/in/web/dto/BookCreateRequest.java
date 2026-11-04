package com.sprint.deokhugam.book.adapter.in.web.dto;

import java.time.LocalDate;

public record BookCreateRequest(
    String title,
    String author,
    String description,
    String publisher,
    LocalDate publishedDate,
    String isbn
) {
}
