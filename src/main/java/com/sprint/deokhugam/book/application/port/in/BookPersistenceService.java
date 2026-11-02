package com.sprint.deokhugam.book.application.port.in;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.book.adapter.in.web.dto.BookCreateRequest;
import com.sprint.deokhugam.book.application.port.out.BookRepositoryPort;
import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.book.domain.exception.BookErrorCode;
import com.sprint.deokhugam.book.domain.exception.BookException;
import com.sprint.deokhugam.user.application.port.out.UserRepositoryPort;
import com.sprint.deokhugam.user.domain.User;
import com.sprint.deokhugam.user.domain.exception.UserErrorCode;
import com.sprint.deokhugam.user.domain.exception.UserException;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookPersistenceService {
    private final BookRepositoryPort bookRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    @Transactional
    public Book saveBook(
        UUID userId,
        BookCreateRequest request,
        String isbn,
        String thumbnailUrl
    ) {
        log.info("도서 DB 저장 실행. userId : {}, isbn : {}", userId, isbn);

        // 1. ISBN 중복 재검증
        if (bookRepositoryPort.existsByIsbn(isbn)) {
            log.warn("도서 등록 실패 : ISBN 중복. isbn : {}", isbn);
            throw new BookException(BookErrorCode.BOOK_ISBN_DUPLICATE);
        }

        // 2. 사용자 존재 여부 재검증
        User user = userRepositoryPort.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> {
                log.warn("도서 등록 실패 : 사용자를 찾을 수 없음. userId : {}", userId);
                return new UserException(UserErrorCode.USER_NOT_FOUND);
            });

        // 3. 도서 엔티티 생성
        Book book = Book.create(
            request.title(),
            user,
            request.author(),
            request.description(),
            request.publisher(),
            request.publishedDate(),
            isbn,
            thumbnailUrl
        );

        // 4. 도서 저장 및 변경 사항 반영
        Book savedBook = bookRepositoryPort.saveAndFlush(book);
        log.info("도서 등록 성공 : bookId : {}, userId : {}, isbn : {}", savedBook.getId(), userId, isbn);

        return savedBook;
    }

    @Transactional
    public void deleteBook(UUID bookId) {
        log.info("도서 논리 삭제 실행. bookId : {}", bookId);

        // 1. 삭제되지 않은 도서 조회
        Book book = bookRepositoryPort.findByIdAndIsDeletedFalse(bookId)
            .orElseThrow(() -> {
                log.warn("도서 삭제 실패 : 도서를 찾을 수 없음. bookId : {}", bookId);

                return new BookException(BookErrorCode.BOOK_NOT_FOUND);
            });

        // 2. 논리 삭제 처리
        book.delete();
        log.info("도서 논리 삭제 성공 : bookId : {}", bookId);
    }
}
