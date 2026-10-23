package com.sprint.deokhugam.comment.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentCreateRequest;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentDto;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentListRequest;
import com.sprint.deokhugam.common.exception.ErrorResponse;
import com.sprint.deokhugam.common.pagination.CursorPageResponse;

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

    @Operation(summary = "댓글 목록 조회",
        description = "리뷰의 댓글 목록을 생성 시간순으로 커서 페이지네이션하여 조회합니다. 논리 삭제된 댓글은 제외됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = CursorPageResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 방향 오류, 페이지네이션 파라미터 오류, 리뷰 ID 누락)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "리뷰 정보 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    CursorPageResponse<CommentDto> getList(CommentListRequest request);
}
