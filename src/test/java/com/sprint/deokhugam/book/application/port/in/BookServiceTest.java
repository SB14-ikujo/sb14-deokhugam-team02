package com.sprint.deokhugam.book.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import com.sprint.deokhugam.book.adapter.in.web.dto.BookCreateRequest;
import com.sprint.deokhugam.book.adapter.in.web.dto.BookDto;
import com.sprint.deokhugam.book.adapter.in.web.mapper.BookMapper;
import com.sprint.deokhugam.book.application.port.out.BookRepositoryPort;
import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.book.domain.exception.BookErrorCode;
import com.sprint.deokhugam.book.domain.exception.BookException;
import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.user.application.port.out.UserRepositoryPort;
import com.sprint.deokhugam.user.domain.User;
import com.sprint.deokhugam.user.domain.exception.UserException;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    private static final Instant CREATED_AT = Instant.parse("2026-10-01T00:00:00Z");

    @Mock
    private BookRepositoryPort bookRepository;

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookService bookService;

    private UUID userId;
    private BookCreateRequest request;
    private User user;
    private BookDto expectedDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        request = new BookCreateRequest(
            "원피스 99: 밀짚모자 루피 (밀짚모자 루피)",
            "오다 에이치로",
            "간부들의 맹추격을 동료들이 저지하고, 옥상을 향한 루피는 마침내 카이도에게?! "
                + "큰 무대에 모인 배우들의 격전이 지금, 시작되려고 한다... 오니가시마 결전 최대 히트 업!! "
                + "'원피스'를 둘러싼 해양 모험 로망!!",
            "대원씨아이",
            LocalDate.of(2023, 8, 16),
            "9791168945456"
        );

        user = org.mockito.Mockito.mock(User.class);

        expectedDto = new BookDto(
            UUID.randomUUID(),
            request.title(),
            request.author(),
            request.description(),
            request.publisher(),
            request.publishedDate(),
            request.isbn(),
            null,
            0,
            0.0,
            Instant.now(),
            Instant.now()
        );

        // 목 매퍼는 기본적으로 null을 반환하므로, 목록 조회 테스트에서 실제 매핑처럼 동작하도록 스텁한다
        lenient().when(bookMapper.toDto(any(Book.class)))
            .thenAnswer(invocation -> toDto(invocation.getArgument(0)));
    }

    private BookDto toDto(Book book) {
        return new BookDto(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getDescription(),
            book.getPublisher(),
            book.getPublishedDate(),
            book.getIsbn(),
            book.getThumbnailUrl(),
            book.getReviewCount(),
            book.getRating(),
            book.getCreatedAt(),
            book.getUpdatedAt()
        );
    }

    @Test
    @DisplayName("정상적인 요청이면 도서를 등록하고 DTO를 반환한다.")
    void createBookSuccess() {
        // given
        when(bookRepository.existsByIsbn(request.isbn()))
            .thenReturn(false);

        when(userRepository.findByIdAndIsDeletedFalse(userId))
            .thenReturn(Optional.of(user));

        when(bookRepository.saveAndFlush(any(Book.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(bookMapper.toDto(any(Book.class)))
            .thenReturn(expectedDto);

        // when
        BookDto result = bookService.createBook(userId, request);

        // then
        assertThat(result).isEqualTo(expectedDto);

        verify(bookRepository).existsByIsbn(request.isbn());
        verify(userRepository).findByIdAndIsDeletedFalse(userId);

        // saveAndFlush()에 전달된 Book 객체 확인
        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);

        verify(bookRepository).saveAndFlush(bookCaptor.capture());

        Book savedBook = bookCaptor.getValue();

        assertThat(savedBook.getTitle()).isEqualTo(request.title());
        assertThat(savedBook.getAuthor()).isEqualTo(request.author());
        assertThat(savedBook.getIsbn()).isEqualTo(request.isbn());
        assertThat(savedBook.getUser()).isSameAs(user);

        verify(bookMapper).toDto(savedBook);
    }

    @Test
    @DisplayName("ISBN 형식이 잘못되면 예외가 발생하고 저장하지 않는다.")
    void createBookWithInvalidIsbn() {
        // given
        BookCreateRequest invalidRequest = new BookCreateRequest(
            "원피스 99: 밀짚모자 루피 (밀짚모자 루피)",
            "오다 에이치로",
            "간부들의 맹추격을 동료들이 저지하고, 옥상을 향한 루피는 마침내 카이도에게?! "
                + "큰 무대에 모인 배우들의 격전이 지금, 시작되려고 한다... 오니가시마 결전 최대 히트 업!! "
                + "'원피스'를 둘러싼 해양 모험 로망!!",
            "대원씨아이",
            LocalDate.of(2023, 8, 16),
            "12345"
        );

        // when & then
        assertThatThrownBy(() -> bookService.createBook(userId, invalidRequest))
            .isInstanceOf(BookException.class)
            .hasMessage(BookErrorCode.BOOK_INVALID_ISBN.getMessage());

        verify(bookRepository, never()).existsByIsbn(any(String.class));
        verify(bookRepository, never()).saveAndFlush(any(Book.class));
    }

    @Test
    @DisplayName("이미 등록된 ISBN이면 예외가 발생하고 저장하지 않는다.")
    void createBookWithDuplicateIsbn() {
        // given
        when(bookRepository.existsByIsbn(request.isbn()))
            .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> bookService.createBook(userId, request))
            .isInstanceOf(BookException.class)
            .hasMessage(BookErrorCode.BOOK_ISBN_DUPLICATE.getMessage());

        verify(bookRepository).existsByIsbn(request.isbn());
        verify(userRepository, never()).findByIdAndIsDeletedFalse(any(UUID.class));
        verify(bookRepository, never()).saveAndFlush(any(Book.class));
    }

    @Test
    @DisplayName("사용자를 찾을 수 없으면 예외가 발생하고 저장하지 않는다.")
    void createBookWithNonexistenUser() {
        // given
        when(bookRepository.existsByIsbn(request.isbn()))
            .thenReturn(false);

        when(userRepository.findByIdAndIsDeletedFalse(userId))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookService.createBook(userId, request))
            .isInstanceOf(UserException.class);

        verify(bookRepository, never()).saveAndFlush(any(Book.class));
    }

    @Test
    @DisplayName("하이픈이 포함된 ISBN은 정규화된 값으로 중복 확인하고 저장한다.")
    void createBookWithHyphenatedIsbn() {
        // given
        BookCreateRequest hyphenatedRequest = new BookCreateRequest(
            "원피스 99: 밀짚모자 루피 (밀짚모자 루피)",
            "오다 에이치로",
            "간부들의 맹추격을 동료들이 저지하고, 옥상을 향한 루피는 마침내 카이도에게?! "
                + "큰 무대에 모인 배우들의 격전이 지금, 시작되려고 한다... 오니가시마 결전 최대 히트 업!! "
                + "'원피스'를 둘러싼 해양 모험 로망!!",
            "대원씨아이",
            LocalDate.of(2023, 8, 16),
            "979-116894-545-6"
        );

        String normalizedIsbn = "9791168945456";

        when(bookRepository.existsByIsbn(normalizedIsbn))
            .thenReturn(false);

        when(userRepository.findByIdAndIsDeletedFalse(userId))
            .thenReturn(Optional.of(user));

        when(bookRepository.saveAndFlush(any(Book.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(bookMapper.toDto(any(Book.class)))
            .thenReturn(expectedDto);

        // when
        bookService.createBook(userId, hyphenatedRequest);

        // then
        verify(bookRepository).existsByIsbn(normalizedIsbn);

        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);

        verify(bookRepository).saveAndFlush(bookCaptor.capture());

        assertThat(bookCaptor.getValue().getIsbn())
            .isEqualTo(normalizedIsbn);
    }

    private Book book(String title, LocalDate publishedDate, double rating, int reviewCount, Instant createdAt) {
        Book book = Book.create(title, mock(User.class), "저자", "설명", "출판사",
            publishedDate, "isbn-" + title, null);
        ReflectionTestUtils.setField(book, "rating", rating);
        ReflectionTestUtils.setField(book, "reviewCount", reviewCount);
        ReflectionTestUtils.setField(book, "createdAt", createdAt);
        return book;
    }

    private Book book(String title) {
        return book(title, LocalDate.of(2020, 1, 1), 4.0, 1, CREATED_AT);
    }

    @Test
    @DisplayName("조회 결과가 limit 이하이면 다음 페이지가 없고 커서는 null이다")
    void getBooks_noNext() {
        given(bookRepository.countByKeyword(null)).willReturn(2L);
        given(bookRepository.findAllByCursor(null, "title", Sort.Direction.ASC, null, null, 5))
            .willReturn(List.of(book("A"), book("B")));

        CursorPageResponse<BookDto> response =
            bookService.getBooks(null, "title", Sort.Direction.ASC, null, null, 5);

        assertThat(response.content()).extracting(BookDto::title).containsExactly("A", "B");
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
        assertThat(response.nextAfter()).isNull();
        assertThat(response.totalElements()).isEqualTo(2L);
    }

    @Test
    @DisplayName("limit보다 1건 더 조회되면 limit건만 반환하고 다음 페이지가 있다")
    void getBooks_hasNext() {
        Instant secondCreatedAt = CREATED_AT.plusSeconds(60);
        given(bookRepository.countByKeyword(null)).willReturn(3L);
        given(bookRepository.findAllByCursor(null, "title", Sort.Direction.ASC, null, null, 2))
            .willReturn(List.of(
                book("A"),
                book("B", LocalDate.of(2020, 1, 1), 4.0, 1, secondCreatedAt),
                book("C")));

        CursorPageResponse<BookDto> response =
            bookService.getBooks(null, "title", Sort.Direction.ASC, null, null, 2);

        assertThat(response.content()).extracting(BookDto::title).containsExactly("A", "B");
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isEqualTo("B");        // 마지막 항목 기준
        assertThat(response.nextAfter()).isEqualTo(secondCreatedAt);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(3L);
    }

    @ParameterizedTest(name = "orderBy={0} → nextCursor={1}")
    @CsvSource({
        "title,T",
        "publishedDate,2020-01-02",
        "rating,4.5",
        "reviewCount,7"
    })
    @DisplayName("정렬 기준에 맞는 값이 다음 커서로 만들어진다")
    void getBooks_cursorByOrderBy(String orderBy, String expectedCursor) {
        Book first = book("T", LocalDate.of(2020, 1, 2), 4.5, 7, CREATED_AT);
        Book extra = book("U", LocalDate.of(2020, 1, 3), 1.0, 1, CREATED_AT);
        given(bookRepository.countByKeyword(null)).willReturn(2L);
        given(bookRepository.findAllByCursor(null, orderBy, Sort.Direction.DESC, null, null, 1))
            .willReturn(List.of(first, extra));

        CursorPageResponse<BookDto> response =
            bookService.getBooks(null, orderBy, Sort.Direction.DESC, null, null, 1);

        assertThat(response.nextCursor()).isEqualTo(expectedCursor);
        assertThat(response.nextAfter()).isEqualTo(CREATED_AT);
    }

    @Test
    @DisplayName("알 수 없는 정렬 기준이면 제목 기준 커서를 사용한다")
    void getBooks_unknownOrderByUsesTitleCursor() {
        given(bookRepository.countByKeyword(null)).willReturn(2L);
        given(bookRepository.findAllByCursor(null, "unknown", Sort.Direction.ASC, null, null, 1))
            .willReturn(List.of(book("A"), book("B")));

        CursorPageResponse<BookDto> response =
            bookService.getBooks(null, "unknown", Sort.Direction.ASC, null, null, 1);

        assertThat(response.nextCursor()).isEqualTo("A");
    }

    @Test
    @DisplayName("조회 조건을 그대로 리포지토리에 전달한다")
    void getBooks_passesConditions() {
        Instant after = Instant.parse("2026-10-02T00:00:00Z");
        given(bookRepository.countByKeyword("자바")).willReturn(0L);
        given(bookRepository.findAllByCursor("자바", "rating", Sort.Direction.DESC, "4.5", after, 20))
            .willReturn(List.of());

        bookService.getBooks("자바", "rating", Sort.Direction.DESC, "4.5", after, 20);

        then(bookRepository).should().findAllByCursor("자바", "rating", Sort.Direction.DESC, "4.5", after, 20);
        then(bookRepository).should().countByKeyword("자바");   // 총 개수는 커서와 무관하게 키워드로만 센다
    }

    @Test
    @DisplayName("조회 결과가 없으면 빈 목록과 총 개수 0을 반환한다")
    void getBooks_empty() {
        given(bookRepository.countByKeyword("없는책")).willReturn(0L);
        given(bookRepository.findAllByCursor("없는책", "title", Sort.Direction.DESC, null, null, 20))
            .willReturn(List.of());

        CursorPageResponse<BookDto> response =
            bookService.getBooks("없는책", "title", Sort.Direction.DESC, null, null, 20);

        assertThat(response.content()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
        assertThat(response.totalElements()).isZero();
    }
}
