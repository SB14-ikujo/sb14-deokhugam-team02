package com.sprint.deokhugam.review.adapter.in.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewLikeDto;
import com.sprint.deokhugam.review.application.port.in.ReviewLikeUseCase;

@RestController
@RequiredArgsConstructor
public class ReviewLikeController {
    private final ReviewLikeUseCase reviewLikeUseCase;

    // '좋아요' 토글
    @RequestMapping(method = RequestMethod.POST, value = "/api/review/{reviewId}/like")
    public ResponseEntity<ReviewLikeDto> create(
        @PathVariable UUID reviewId,
        @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {
        ReviewLikeDto responseDto = reviewLikeUseCase.toggleReviewLike(reviewId, requestUserId);
        return ResponseEntity.ok(responseDto);
    }
}
