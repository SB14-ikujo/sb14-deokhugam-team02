package com.sprint.deokhugam.review.adapter.in.web.dto;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewCreateRequest(
    @NotNull
    UUID bookId,

    @NotNull
    UUID userId,

    @NotBlank
    String content,

    @NotNull
    @Min(1)
    @Max(5)
    Integer rating
) {
}
