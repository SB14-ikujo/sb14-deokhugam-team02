package com.sprint.deokhugam.book.application.port.out;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.sprint.deokhugam.book.domain.Book;

public interface BookRepositoryPort extends JpaRepository<Book, UUID>, BookRepositoryCustom {
    Optional<Book> findByIdAndIsDeletedFalse(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.id = :id AND b.isDeleted = false")
    Optional<Book> findByIdAndIsDeletedFalseForUpdate(@Param("id") UUID id);

    boolean existsByIsbn(String isbn);
}
