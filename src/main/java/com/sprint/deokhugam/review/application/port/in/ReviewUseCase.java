package com.sprint.deokhugam.review.application.port.in;

import java.util.UUID;

import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewCreateRequest;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewDto;

public interface ReviewUseCase {
    ReviewDto createReview(ReviewCreateRequest createRequest);

    void reviewSoftDelete(UUID reviewId, UUID requestUserId);
}
