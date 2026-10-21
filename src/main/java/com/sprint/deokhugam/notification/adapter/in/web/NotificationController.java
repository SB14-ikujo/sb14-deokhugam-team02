package com.sprint.deokhugam.notification.adapter.in.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.common.context.user.RequestUserId;
import com.sprint.deokhugam.notification.adapter.in.web.dto.NotificationDto;
import com.sprint.deokhugam.notification.application.port.in.NotificationUseCase;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationUseCase notificationUseCase;

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/api/notifications/{notificationId}")
    public NotificationDto markAsRead(
        @PathVariable UUID notificationId,
        @RequestUserId UUID userId
    ) {
        return notificationUseCase.markAsRead(notificationId, userId);
    }

}
