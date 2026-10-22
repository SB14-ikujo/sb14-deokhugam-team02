package com.sprint.deokhugam.comment.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentCreateRequest;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentDto;
import com.sprint.deokhugam.common.exception.ErrorResponse;

@Tag(name = "댓글 관리", description = "댓글 관련 API")
public interface CommentApi {

    @Operation(summary = "댓글 등록", description = "새로운 댓글을 등록합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "댓글 등록 성공",
            content = @Content(schema = @Schema(implementation = CommentDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "리뷰 또는 사용자 정보 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    CommentDto create(CommentCreateRequest request);
}
