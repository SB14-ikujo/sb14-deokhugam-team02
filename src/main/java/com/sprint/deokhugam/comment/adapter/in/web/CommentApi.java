package com.sprint.deokhugam.comment.adapter.in.web;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentCreateRequest;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentDto;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentListRequest;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentUpdateRequest;
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
        description = "리뷰의 댓글 목록을 생성 시간순으로 커서 페이지네이션하여 조회. 논리 삭제된 댓글은 제외.")
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
    CursorPageResponse<CommentDto> getList(@ParameterObject CommentListRequest request);

    //댓글 수정 API
    @Operation(summary = "댓글 수정", description = "본인이 작성한 댓글을 수정합니다.")
    @Parameter(name = "commentId", description = "댓글 ID", in = ParameterIn.PATH, required = true,
        example = "123e4567-e89b-12d3-a456-426614174000")
    @Parameter(name = "Deokhugam-Request-User-ID", description = "요청자 ID", in = ParameterIn.HEADER,
        required = true, example = "123e4567-e89b-12d3-a456-426614174000")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "댓글 수정 성공",
            content = @Content(schema = @Schema(implementation = CommentDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패, 요청자 ID 누락)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "댓글 수정 권한 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "댓글 정보 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    CommentDto update(UUID commentId, UUID requestUserId, CommentUpdateRequest request);
}
