package com.sprint.deokhugam.review.application.port.in;

import java.util.UUID;

import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewDto;

public interface ReviewUseCase {
    ReviewDto createReview(UUID bookId, UUID userId, String content, Integer rating);
}
