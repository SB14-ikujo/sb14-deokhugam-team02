package com.sprint.deokhugam.book.application.port.out;

import java.util.List;

import com.sprint.deokhugam.book.application.dto.BookSearchResult;

// 카카오 도서 정보 조회
public interface BookSearchPort {
    List<BookSearchResult> searchByIsbn(String isbn);
}
