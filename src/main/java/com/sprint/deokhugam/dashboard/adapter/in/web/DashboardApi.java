package com.sprint.deokhugam.dashboard.adapter.in.web;

import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.sprint.deokhugam.common.exception.ErrorResponse;
import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularBookDto;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularBookRequest;

public interface DashboardApi {
    @Tag(name = "도서 관리")
    @Operation(
        summary = "인기 도서 목록 조회",
        description = "기간별 인기 도서 목록을 순위 순으로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "인기 도서 목록 조회 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (랭킹 기간, 정렬 방향, 커서, limit 오류)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<CursorPageResponse<PopularBookDto>> getPopularBooks(PopularBookRequest request);
}
