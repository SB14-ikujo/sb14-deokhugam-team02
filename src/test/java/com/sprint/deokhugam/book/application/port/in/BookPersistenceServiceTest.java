package com.sprint.deokhugam.book.application.port.in;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sprint.deokhugam.book.adapter.in.web.dto.BookUpdateRequest;
import com.sprint.deokhugam.book.application.port.out.BookRepositoryPort;
import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.book.domain.exception.BookErrorCode;
import com.sprint.deokhugam.book.domain.exception.BookException;
import com.sprint.deokhugam.user.application.port.out.UserRepositoryPort;
import com.sprint.deokhugam.user.domain.User;

@ExtendWith(MockitoExtension.class)
class BookPersistenceServiceTest {
    @Mock
    private BookRepositoryPort bookRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private BookPersistenceService bookPersistenceService;

    @Test
    @DisplayName("존재하는 도서를 논리 삭제한다.")
    void deleteBookSuccess() {
        // given
        UUID bookId = UUID.randomUUID();

        Book book = Book.create(
            "원피스 99",
            org.mockito.Mockito.mock(User.class),
            "오다 에이치로",
            "도서 설명",
            "대원씨아이",
            LocalDate.of(2023, 8, 16),
            "9791168945456",
            null
        );

        given(bookRepositoryPort.findByIdAndIsDeletedFalse(bookId))
            .willReturn(Optional.of(book));

        // when
        bookPersistenceService.deleteBook(bookId);

        // then
        assertThat(book.isDeleted()).isTrue();

        then(bookRepositoryPort).should()
            .findByIdAndIsDeletedFalse(bookId);

        then(bookRepositoryPort).should(never())
            .save(any(Book.class));
    }

    @Test
    @DisplayName("존재하지 않는 도서를 삭제하면 예외가 발생한다.")
    void deleteBookWithNonexistentBook() {
        // given
        UUID bookId = UUID.randomUUID();

        given(bookRepositoryPort.findByIdAndIsDeletedFalse(bookId))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookPersistenceService.deleteBook(bookId))
            .isInstanceOf(BookException.class)
            .hasMessage(BookErrorCode.BOOK_NOT_FOUND.getMessage());

        then(bookRepositoryPort).should()
            .findByIdAndIsDeletedFalse(bookId);
    }

    @Test
    @DisplayName("이미 논리 삭제된 도서를 다시 삭제하면 예외가 발생한다.")
    void deleteBookAlreadyDeleted() {
        // given
        UUID bookId = UUID.randomUUID();

        Book book = Book.create(
            "원피스 99",
            org.mockito.Mockito.mock(User.class),
            "오다 에이치로",
            "도서 설명",
            "대원씨아이",
            LocalDate.of(2023, 8, 16),
            "9791168945456",
            null
        );

        ReflectionTestUtils.setField(book, "isDeleted", true);

        given(bookRepositoryPort.findByIdAndIsDeletedFalse(bookId))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookPersistenceService.deleteBook(bookId))
            .isInstanceOf(BookException.class)
            .hasMessage(BookErrorCode.BOOK_NOT_FOUND.getMessage());

        then(bookRepositoryPort).should()
            .findByIdAndIsDeletedFalse(bookId);
    }

    @Test
    @DisplayName("정상적인 요청이면 도서 정보를 수정한다.")
    void updateBookSuccess() {
        // given
        UUID bookId = UUID.randomUUID();
        User user = mock(User.class);

        Book book = Book.create(
            "기존 제목",
            user,
            "기존 저자",
            "기존 설명",
            "기존 출판사",
            LocalDate.of(2026, 11, 2),
            "9791168945456",
            "old-thumbnail-key"
        );

        ReflectionTestUtils.setField(book, "id", bookId);

        BookUpdateRequest request = new BookUpdateRequest(
            "수정 제목",
            "수정 저자",
            "수정 설명",
            "수정 출판사",
            LocalDate.of(2026, 11, 3)
        );

        given(bookRepositoryPort.findByIdAndIsDeletedFalse(bookId))
            .willReturn(Optional.of(book));

        // when
        Book result = bookPersistenceService.updateBook(
            bookId,
            request,
            "new-thumbnail-key"
        );

        // then
        assertThat(result.getTitle()).isEqualTo("수정 제목");
        assertThat(result.getAuthor()).isEqualTo("수정 저자");
        assertThat(result.getDescription()).isEqualTo("수정 설명");
        assertThat(result.getPublisher()).isEqualTo("수정 출판사");
        assertThat(result.getPublishedDate()).isEqualTo(LocalDate.of(2026, 11, 3));
        assertThat(result.getThumbnailUrl()).isEqualTo("new-thumbnail-key");

        verify(bookRepositoryPort).findByIdAndIsDeletedFalse(bookId);
        verify(bookRepositoryPort, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("일부 필드만 전달하면 전달된 필드만 수정한다.")
    void updateBookPartially() {
        // given
        UUID bookId = UUID.randomUUID();
        User user = mock(User.class);

        Book book = Book.create(
            "기존 제목",
            user,
            "기존 저자",
            "기존 설명",
            "기존 출판사",
            LocalDate.of(2026, 11, 2),
            "9791168945456",
            "old-thumbnail-key"
        );

        ReflectionTestUtils.setField(book, "id", bookId);

        BookUpdateRequest request = new BookUpdateRequest(
            "수정 제목",
            null,
            null,
            null,
            null
        );

        given(bookRepositoryPort.findByIdAndIsDeletedFalse(bookId))
            .willReturn(Optional.of(book));

        // when
        Book result = bookPersistenceService.updateBook(bookId, request, null);

        // then
        assertThat(result.getTitle()).isEqualTo("수정 제목");
        assertThat(result.getAuthor()).isEqualTo("기존 저자");
        assertThat(result.getDescription()).isEqualTo("기존 설명");
        assertThat(result.getPublisher()).isEqualTo("기존 출판사");
        assertThat(result.getPublishedDate()).isEqualTo(LocalDate.of(2026, 11, 2));
        assertThat(result.getThumbnailUrl()).isEqualTo("old-thumbnail-key");
    }

    @Test
    @DisplayName("존재하지 않는 도서를 수정하면 예외가 발생한다.")
    void updateBookNotFound() {
        // given
        UUID bookId = UUID.randomUUID();

        BookUpdateRequest request = new BookUpdateRequest(
            "수정 제목",
            null,
            null,
            null,
            null
        );

        given(bookRepositoryPort.findByIdAndIsDeletedFalse(bookId))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookPersistenceService.updateBook(bookId, request, null))
            .isInstanceOf(BookException.class)
            .hasMessage(BookErrorCode.BOOK_NOT_FOUND.getMessage());

        verify(bookRepositoryPort).findByIdAndIsDeletedFalse(bookId);
    }

    @Test
    @DisplayName("논리 삭제된 도서를 수정하면 예외가 발생한다.")
    void updateDeletedBook() {
        // given
        UUID bookId = UUID.randomUUID();

        BookUpdateRequest request = new BookUpdateRequest(
            "수정 제목",
            null,
            null,
            null,
            null
        );

        given(bookRepositoryPort.findByIdAndIsDeletedFalse(bookId))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookPersistenceService.updateBook(bookId, request, null))
            .isInstanceOf(BookException.class)
            .hasMessage(BookErrorCode.BOOK_NOT_FOUND.getMessage());
    }
}
