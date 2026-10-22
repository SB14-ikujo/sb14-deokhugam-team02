package com.sprint.deokhugam.notification.application.port.out;

import java.util.UUID;

public interface NotificationRepositoryCustom {

    long markAllAsRead(UUID userId);
}
