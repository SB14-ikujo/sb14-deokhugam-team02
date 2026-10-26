package com.sprint.deokhugam.review.adapter.in.web;

import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.sprint.deokhugam.common.context.user.RequestUserId;
import com.sprint.deokhugam.common.exception.ErrorResponse;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewCreateRequest;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewDto;

@Tag(name = "리뷰 관리", description = "리뷰 관련 API")
public interface ReviewApi {
    @Operation(summary = "리뷰 등록", description = "새로운 리뷰를 등록합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "리뷰 등록 성공",
            content = @Content(schema = @Schema(implementation = ReviewDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "도서 정보 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "이미 작성된 리뷰 존재",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ReviewDto createReview(@Valid @RequestBody ReviewCreateRequest createRequest);

    @Operation(summary = "리뷰 논리 삭제", description = "본인이 작성한 리뷰를 논리적으로 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "리뷰 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "리뷰 삭제 권한 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "리뷰 정보 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    void reviewSoftDelete(@PathVariable UUID reviewId, @RequestUserId UUID requestUserId);

    @Operation(summary = "리뷰 상세 정보 조회", description = "리뷰 ID로 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리뷰 정보 조회 성공",
            content = @Content(schema = @Schema(implementation = ReviewDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "리뷰 정보 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ReviewDto getReviewDetail(@PathVariable UUID reviewId, @RequestUserId UUID requestUserId);
}
