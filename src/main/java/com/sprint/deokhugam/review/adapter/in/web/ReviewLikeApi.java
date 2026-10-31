package com.sprint.deokhugam.review.adapter.in.web;

import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.sprint.deokhugam.common.context.user.RequestUserId;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewLikeDto;

@Tag(name = "리뷰 관리", description = "리뷰 관련 API")
public interface ReviewLikeApi {
    @Operation(summary = "리뷰 좋아요", description = "리뷰에 좋아요를 추가하거나 취소합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리뷰 좋아요 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)"),
        @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ReviewLikeDto create(@PathVariable UUID reviewId, @RequestUserId UUID requestUserId);
}
