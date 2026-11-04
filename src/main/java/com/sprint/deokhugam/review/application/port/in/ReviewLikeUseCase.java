package com.sprint.deokhugam.review.application.port.in;

import java.util.UUID;

import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewLikeDto;

public interface ReviewLikeUseCase {

    ReviewLikeDto reviewLikeToggle(UUID reviewId, UUID userId);
}
