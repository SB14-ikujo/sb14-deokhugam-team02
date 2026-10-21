package com.sprint.deokhugam.user.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
    @NotBlank
    String email,

    @NotBlank
    String password
) {
}
