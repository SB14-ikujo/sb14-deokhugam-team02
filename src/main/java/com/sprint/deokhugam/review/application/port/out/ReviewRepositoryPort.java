package com.sprint.deokhugam.review.application.port.out;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sprint.deokhugam.review.domain.Review;

public interface ReviewRepositoryPort extends JpaRepository<Review, UUID> {
    Optional<Review> findByUserIdAndBookId(UUID userId, UUID bookId);
}
