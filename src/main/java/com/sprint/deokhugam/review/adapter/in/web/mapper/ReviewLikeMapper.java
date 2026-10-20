package com.sprint.deokhugam.review.adapter.in.web.mapper;

import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewLikeDto;
import com.sprint.deokhugam.review.domain.ReviewLike;

public class ReviewLikeMapper {
    public static ReviewLikeDto toResponseDto(ReviewLike reviewLike, boolean liked) {
        return new ReviewLikeDto(
            reviewLike.getReview().getId(),
            reviewLike.getUser().getId(),
            liked
        );
    }
}
