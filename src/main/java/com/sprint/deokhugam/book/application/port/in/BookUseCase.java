package com.sprint.deokhugam.book.application.port.in;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import com.sprint.deokhugam.book.adapter.in.web.dto.BookCreateRequest;
import com.sprint.deokhugam.book.adapter.in.web.dto.BookDto;
import com.sprint.deokhugam.common.pagination.CursorPageResponse;

public interface BookUseCase {

    BookDto createBook(UUID userId, BookCreateRequest request);

    CursorPageResponse<BookDto> getBooks(
        String keyword, String orderBy, Sort.Direction direction,
        String cursor, Instant after, Integer limit);
}
