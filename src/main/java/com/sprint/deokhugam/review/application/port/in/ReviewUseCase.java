package com.sprint.deokhugam.review.application.port.in;

import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewCreateRequest;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewDto;

public interface ReviewUseCase {
    ReviewDto createReview(ReviewCreateRequest createRequest);
}
