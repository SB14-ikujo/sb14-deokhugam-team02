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
}
