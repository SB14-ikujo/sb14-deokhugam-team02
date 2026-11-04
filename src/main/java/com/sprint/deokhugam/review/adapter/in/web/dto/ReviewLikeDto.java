package com.sprint.deokhugam.review.adapter.in.web.dto;

import java.util.UUID;

public record ReviewLikeDto(
    UUID reviewId,
    UUID userId,
    boolean liked
) {
}
