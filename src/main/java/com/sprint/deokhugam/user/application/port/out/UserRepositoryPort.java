package com.sprint.deokhugam.user.application.port.out;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sprint.deokhugam.user.domain.User;

public interface UserRepositoryPort extends JpaRepository<User, UUID> {
}
