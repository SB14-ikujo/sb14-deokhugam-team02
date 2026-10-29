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

    // 조건(유저, 도서, 키워드)에 맞는 전체 리뷰의 총 개수를 세는 메서드
    long countByConditions(UUID userId, UUID bookId, String keyword);
}
