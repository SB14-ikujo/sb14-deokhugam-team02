package com.sprint.deokhugam.review.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewCreateRequest;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewDto;
import com.sprint.deokhugam.review.application.port.in.ReviewUseCase;

@RestController
@RequiredArgsConstructor
public class ReviewController implements ReviewApi {

    private final ReviewUseCase reviewUseCase;

    // 리뷰 생성
    @RequestMapping(method = RequestMethod.POST, value = "/api/reviews")
    public ResponseEntity<ReviewDto> createReview(@Valid @RequestBody ReviewCreateRequest createRequest) {
        ReviewDto response = reviewUseCase.createReview(createRequest);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }
}
