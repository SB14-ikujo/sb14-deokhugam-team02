package com.sprint.deokhugam.book.application.port.in;

import java.util.UUID;

import com.sprint.deokhugam.book.domain.Book;

public interface BookUseCase {
    //  ++ 도서 상세 조회
    Book findById(UUID bookId);
}
