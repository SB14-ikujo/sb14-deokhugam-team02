package com.sprint.deokhugam.review.application.port.in;

import java.util.UUID;

import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewCreateRequest;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewDto;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewListRequest;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewUpdateRequest;

public interface ReviewUseCase {
    ReviewDto createReview(ReviewCreateRequest createRequest);

    void reviewSoftDelete(UUID reviewId, UUID requestUserId);

    void reviewHardDelete(UUID reviewId, UUID requestUserId);

    ReviewDto getReviewDetail(UUID reviewId, UUID requestUserId);

    ReviewDto updateReview(UUID reviewId, UUID requestUserId, ReviewUpdateRequest updateRequest);

    CursorPageResponse<ReviewDto> getReviewList(ReviewListRequest request, UUID requestUserId);
}
