package com.sprint.deokhugam.user.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

public record UserDto(
    UUID id,
    String email,
    String nickname,
    Instant createdAt
) {
}
