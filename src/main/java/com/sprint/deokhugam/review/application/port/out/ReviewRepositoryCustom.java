package com.sprint.deokhugam.review.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import com.sprint.deokhugam.review.domain.Review;

public interface ReviewRepositoryCustom {
    List<Review> getReviewList(
        UUID userId,
        UUID bookId,
        String keyword,
        String orderBy,
        Instant after,
        String cursor,
        Sort.Direction direction,
        int size
    );

    long countByConditions(UUID userId, UUID bookId, String keyword);
}
