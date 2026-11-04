package com.sprint.deokhugam.book.application.port.out;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Sort;

import com.sprint.deokhugam.book.domain.Book;

public interface BookRepositoryCustom {

    List<Book> findAllByCursor(
        String keyword, String orderBy,
        Sort.Direction direction, String cursor, Instant after, Integer limit
    );

    Long countByKeyword(String keyword);
}
