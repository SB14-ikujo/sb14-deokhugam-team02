package com.sprint.deokhugam.book.application.port.in;

import java.util.UUID;

import com.sprint.deokhugam.book.adapter.in.web.dto.BookCreateRequest;
import com.sprint.deokhugam.book.adapter.in.web.dto.BookDto;

public interface BookUseCase {

    BookDto createBook(UUID userId, BookCreateRequest request);
}
