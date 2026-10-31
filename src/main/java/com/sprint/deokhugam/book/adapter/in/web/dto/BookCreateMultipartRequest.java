package com.sprint.deokhugam.book.adapter.in.web.dto;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "도서 등록 요청")
public record BookCreateMultipartRequest(
    @Schema(
        description = "등록할 도서 정보",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    BookCreateRequest bookData,

    @Schema(
        description = "도서 썸네일 이미지",
        type = "string",
        format = "binary"
    )
    MultipartFile thumbnailImage
) {
}
