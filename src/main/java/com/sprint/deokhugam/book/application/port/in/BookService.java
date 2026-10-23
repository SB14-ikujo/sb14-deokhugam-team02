package com.sprint.deokhugam.book.application.port.in;

import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.book.application.port.out.BookRepositoryPort;
import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.book.domain.exception.BookErrorCode;
import com.sprint.deokhugam.book.domain.exception.BookException;

@Service
@RequiredArgsConstructor
public class BookService implements BookUseCase {

    private final BookRepositoryPort bookRepositoryPort;

    // ++ 도서 상세 조회 => 한번 보고 마음에 안드시면 바꾸셔도 됩니다.
    public Book findById(UUID bookId) {
        return bookRepositoryPort.findById(bookId)
            .orElseThrow(() -> new BookException(BookErrorCode.BOOK_NOT_FOUND));
    }
}
