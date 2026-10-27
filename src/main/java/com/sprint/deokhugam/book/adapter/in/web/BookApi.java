package com.sprint.deokhugam.book.adapter.in.web;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.sprint.deokhugam.book.adapter.in.web.dto.BookCreateRequest;
import com.sprint.deokhugam.book.adapter.in.web.dto.BookDto;
import com.sprint.deokhugam.book.adapter.in.web.dto.BookSearchRequest;
import com.sprint.deokhugam.common.exception.ErrorResponse;
import com.sprint.deokhugam.common.pagination.CursorPageResponse;

/** Swagger 문서 전용. {@link BookController} */
@Tag(name = "도서 관리", description = "도서 관련 API")
public interface BookApi {

    @Operation(
        summary = "도서 등록",
        description = "새로운 도서를 등록합니다."
    )
    @Parameter(
        name = "Deokhugam-Request-User_ID", description = "요청자 ID",
        in = ParameterIn.HEADER, required = true,
        example = "123e4567-e89b-12d3-a456-426614174000",
        schema = @Schema(type = "string", format = "uuid")
    )
    @RequestBody(
        description = "등록할 도서 정보",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BookCreateRequest.class)
        )
    )
    @ApiResponse(responseCode = "201", description = "도서 등록 성공",
        content = @Content(schema = @Schema(implementation = BookDto.class))
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 ISBN 형식 오류",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "요청한 사용자 정보 없음",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(responseCode = "409", description = "이미 등록된 ISBN",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    BookDto createBook(UUID userId, @Valid BookCreateRequest request);

    @Operation(
        summary = "도서 목록 조회",
        description = "도서 제목, 저자, ISBN으로 검색하고 정렬 기준에 따라 커서 페이지네이션하여 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "도서 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 기준 또는 방향 오류, 페이지네이션 파라미터 오류)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    CursorPageResponse<BookDto> getBooks(@ParameterObject BookSearchRequest request);
}
