package com.sprint.deokhugam.notification.application.port.out;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sprint.deokhugam.notification.domain.Notification;

public interface NotificationRepositoryPort extends JpaRepository<Notification, UUID> {
}
