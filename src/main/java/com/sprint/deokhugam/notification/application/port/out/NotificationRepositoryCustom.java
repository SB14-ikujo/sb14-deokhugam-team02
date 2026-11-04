package com.sprint.deokhugam.notification.application.port.out;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import com.sprint.deokhugam.notification.domain.Notification;

public interface NotificationRepositoryCustom {

    long markAllAsRead(UUID userId);

    Long countByUserId(UUID userId);

    List<Notification> findAllByUserIdWithCursor(UUID userId, Sort.Direction direction, Instant cursor, Instant after,
        Integer limit);

    long deleteReadNotificationsBefore(LocalDate baseDate);
}
