package com.sprint.deokhugam.review.application.port.in;

import java.util.UUID;

import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewLikeDto;

public interface ReviewLikeUseCase {
    // '좋아요' toggle
    ReviewLikeDto toggleReviewLike(UUID reviewId, UUID requestId);
}
