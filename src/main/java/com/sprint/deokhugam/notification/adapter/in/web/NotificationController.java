package com.sprint.deokhugam.notification.adapter.in.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.common.context.user.RequestUserId;
import com.sprint.deokhugam.notification.adapter.in.web.dto.NotificationDto;
import com.sprint.deokhugam.notification.adapter.in.web.dto.NotificationUpdateRequest;
import com.sprint.deokhugam.notification.application.port.in.NotificationUseCase;

@Slf4j
@RestController
@RequestMapping(value = "/api/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {
    private final NotificationUseCase notificationUseCase;

    @Override
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/{notificationId}")
    public NotificationDto markAsRead(
        @PathVariable UUID notificationId,
        @Parameter(hidden = true) @RequestUserId UUID userId,
        @RequestBody @Valid NotificationUpdateRequest updateRequest
    ) {
        return notificationUseCase.markAsRead(notificationId, userId, updateRequest.confirmed());
    }

}
