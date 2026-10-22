package com.sprint.deokhugam.notification.adapter.in.web;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.sprint.deokhugam.common.exception.ErrorResponse;
import com.sprint.deokhugam.notification.adapter.in.web.dto.NotificationDto;
import com.sprint.deokhugam.notification.adapter.in.web.dto.NotificationUpdateRequest;

/** Swagger 문서 전용. {@link NotificationController}*/
@Tag(name = "알림 관리", description = "알림 관련 API")
public interface NotificationApi {

    @Operation(
        summary = "알림 읽음 처리",
        description = "특정 알림의 읽음 상태를 업데이트합니다."
    )
    @Parameter(
        name = "notificationId", description = "알림 ID", in = ParameterIn.PATH,
        required = true, example = "123e4567-e89b-12d3-a456-426614174000"
    )
    @Parameter(
        name = "Deokhugam-Request-User-ID", description = "요청자 ID",
        in = ParameterIn.HEADER, required = true,
        example = "123e4567-e89b-12d3-a456-426614174000",
        schema = @Schema(type = "string", format = "uuid")
    )
    @RequestBody(
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = NotificationUpdateRequest.class)
        ),
        required = true
    )
    @ApiResponse(responseCode = "200", description = "알림 상태 업데이트 성공",
        content = @Content(schema = @Schema(implementation = NotificationDto.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패, 요청자 ID 누락)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "알림 수정 권한 없음",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "알림 정보 없음",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    NotificationDto markAsRead(UUID notificationId, UUID userId, NotificationUpdateRequest updateRequest);

}
