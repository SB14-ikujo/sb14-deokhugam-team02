package com.sprint.deokhugam.book.application.port.in;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
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
import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.common.util.SliceUtils;
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
                return new UserException(UserErrorCode.USER_NOT_FOUND);
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

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<BookDto> getBooks(
        String keyword, String orderBy, Sort.Direction direction,
        String cursor, Instant after, Integer limit
    ) {

        long bookTotalCount = bookRepositoryPort.countByKeyword(keyword);
        log.info("도서 목록 조회 정보 -keyword: {}, orderBy:{}, direction:{}, cursor:{}, after:{}, limit:{}",
            keyword, orderBy, direction, cursor, after, limit
        );

        this.validateCursor(orderBy, cursor);

        List<BookDto> books = bookRepositoryPort
            .findAllByCursor(keyword, orderBy, direction, cursor, after, limit)
            .stream().map(bookMapper::toDto)
            .toList();

        Slice<BookDto> slice = SliceUtils.toSlice(books, limit);

        Function<BookDto, String> cursorExtractor = switch (orderBy) {
            case "publishedDate" -> dto -> dto.publishedDate().toString();  // 출판일순
            case "rating" -> dto -> String.valueOf(dto.rating());           // 평점
            case "reviewCount" -> dto -> String.valueOf(dto.reviewCount()); // 리뷰
            default -> BookDto::title;                                      // 제목순
        };

        log.info("총 데이터 : {}", bookTotalCount);
        log.info(
            "최종 데이터 정보 [content_size: {} hasNext : {}]",
            slice.getNumberOfElements(), slice.hasNext()
        );

        return CursorPageResponse.of(
            slice, bookTotalCount,
            cursorExtractor,
            BookDto::createdAt
        );
    }

    private void validateCursor(String orderBy, String cursor) {
        if (cursor == null) {
            return;
        }
        try {
            switch (orderBy) {
                case "publishedDate" -> LocalDate.parse(cursor);
                case "rating" -> Double.parseDouble(cursor);
                case "reviewCount" -> Integer.parseInt(cursor);
                default -> {
                }
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            log.warn("도서 목록 조회 실패 : 잘못된 cursor - orderBy: {}, cursor: {}", orderBy, cursor);
            throw new BookException(BookErrorCode.BOOK_INVALID_CURSOR);
        }
    }
}
