package com.sprint.deokhugam.notification.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "알림 상태 정보")
public record NotificationUpdateRequest(
    @NotNull(message = "알림 상태 정보는 필수값 입니다.")
    Boolean confirmed
) {
}
