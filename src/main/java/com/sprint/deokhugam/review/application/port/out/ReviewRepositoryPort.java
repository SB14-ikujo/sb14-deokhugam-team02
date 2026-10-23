package com.sprint.deokhugam.review.application.port.out;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.user.domain.User;

public interface ReviewRepositoryPort extends JpaRepository<Review, UUID> {
    Optional<Review> findByUserAndBook(User user, Book book);
}
