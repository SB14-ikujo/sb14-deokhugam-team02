package com.sprint.deokhugam.review.application.port.in;

import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewCreateRequest;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewDto;

import java.util.UUID;

public interface ReviewUseCase {
    ReviewDto createReview(ReviewCreateRequest createRequest);
    void reviewSoftDelete(UUID reviewId);
}
