package com.sprint.deokhugam.book.application.port.in;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sprint.deokhugam.book.adapter.in.web.dto.BookCreateRequest;
import com.sprint.deokhugam.book.adapter.in.web.dto.BookDto;
import com.sprint.deokhugam.book.adapter.in.web.mapper.BookMapper;
import com.sprint.deokhugam.book.application.port.out.BookRepositoryPort;
import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.book.domain.exception.BookErrorCode;
import com.sprint.deokhugam.book.domain.exception.BookException;
import com.sprint.deokhugam.user.application.port.out.UserRepositoryPort;
import com.sprint.deokhugam.user.domain.User;
import com.sprint.deokhugam.user.domain.exception.UserException;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
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
}
