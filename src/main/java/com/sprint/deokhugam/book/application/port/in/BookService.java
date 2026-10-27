package com.sprint.deokhugam.book.application.port.in;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.book.adapter.in.web.dto.BookCreateRequest;
import com.sprint.deokhugam.book.adapter.in.web.dto.BookDto;
import com.sprint.deokhugam.book.adapter.in.web.mapper.BookMapper;
import com.sprint.deokhugam.book.application.port.out.BookRepositoryPort;
import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.book.domain.exception.BookErrorCode;
import com.sprint.deokhugam.book.domain.exception.BookException;
import com.sprint.deokhugam.book.domain.validator.IsbnValidator;
import com.sprint.deokhugam.user.application.port.out.UserRepositoryPort;
import com.sprint.deokhugam.user.domain.User;
import com.sprint.deokhugam.user.domain.exception.UserErrorCode;
import com.sprint.deokhugam.user.domain.exception.UserException;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService implements BookUseCase {
    private final BookRepositoryPort bookRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final BookMapper bookMapper;

    @Override
    @Transactional
    public BookDto createBook(UUID userId, BookCreateRequest request) {
        log.info("도서 등록 요청 : userId : {}", userId);

        String isbn = IsbnValidator.normalizeAndValidate(request.isbn());
        log.debug("ISBN 유효성 검증 완료");

        if (bookRepositoryPort.existsByIsbn(isbn)) {
            log.warn("도서 등록 실패 : ISBN 중복");
            throw new BookException(BookErrorCode.BOOK_ISBN_DUPLICATE);
        }

        User user = userRepositoryPort.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> {
                log.warn("도서 등록 실패 : 사용자를 찾을 수 없음");
                throw new UserException(UserErrorCode.USER_NOT_FOUND);
            });

        Book book = Book.create(
            request.title(),
            user,
            request.author(),
            request.description(),
            request.publisher(),
            request.publishedDate(),
            isbn,
            null
        );

        Book savedBook = bookRepositoryPort.saveAndFlush(book);
        log.info("도서 등록 성공 : bookId : {}, userId : {}", savedBook.getId(), userId);

        BookDto bookDto = bookMapper.toDto(savedBook);

        return bookDto;
    }
}
