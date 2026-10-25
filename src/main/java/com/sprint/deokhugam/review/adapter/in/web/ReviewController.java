package com.sprint.deokhugam.review.adapter.in.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.common.context.user.RequestUserId;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewCreateRequest;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewDto;
import com.sprint.deokhugam.review.application.port.in.ReviewUseCase;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController implements ReviewApi {

    private final ReviewUseCase reviewUseCase;

    // 리뷰 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto createReview(@Valid @RequestBody ReviewCreateRequest createRequest) {
        return reviewUseCase.createReview(createRequest);
    }

    // 리뷰 논리적 삭제
    @DeleteMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reviewSoftDelete(@PathVariable UUID reviewId, @RequestUserId UUID requestUserId) {
        reviewUseCase.reviewSoftDelete(reviewId, requestUserId);
    }

    // 리뷰 상세 조회
    @RequestMapping(method = RequestMethod.GET, value = "/api/reviews/{reviewId}")
    public ResponseEntity<ReviewDto> getReviewDetail(@PathVariable UUID reviewId, @RequestUserId UUID requestUserId) {
        ReviewDto response = reviewUseCase.getReviewDetail(reviewId);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }
}
