package com.sprint.deokhugam.review.adapter.in.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.common.context.user.RequestUserId;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewLikeDto;
import com.sprint.deokhugam.review.application.port.in.ReviewLikeUseCase;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewLikeController implements ReviewLikeApi {
    private final ReviewLikeUseCase reviewLikeUseCase;

    // '좋아요' 토글
    @PostMapping("/{reviewId}/like")
    @ResponseStatus(HttpStatus.OK)
    public ReviewLikeDto create(@PathVariable UUID reviewId, @RequestUserId UUID requestUserId) {
        return reviewLikeUseCase.reviewLikeToggle(reviewId, requestUserId);
    }
}
