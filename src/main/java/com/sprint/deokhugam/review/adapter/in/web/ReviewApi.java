package com.sprint.deokhugam.review.adapter.in.web;

import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.sprint.deokhugam.common.exception.ErrorResponse;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewCreateRequest;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewDto;

@Tag(name = "리뷰 관리", description = "리뷰 관련 API")
public interface ReviewApi {
    @Operation(summary = "리뷰 등록", description = "새로운 리뷰를 등록합니다.")
    @ApiResponse(responseCode = "201", description = "리뷰 등록 성공",
        content = @Content(schema = @Schema(implementation = ReviewDto.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "도서 정보 없음",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "이미 작성된 리뷰 존재",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<ReviewDto> createReview(ReviewCreateRequest createRequest);
}
