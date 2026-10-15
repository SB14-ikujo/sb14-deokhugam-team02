package com.sprint.deokhugam.book.application.port.out;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sprint.deokhugam.book.domain.Book;

public interface BookRepositoryPort extends JpaRepository<Book, UUID> {
}
