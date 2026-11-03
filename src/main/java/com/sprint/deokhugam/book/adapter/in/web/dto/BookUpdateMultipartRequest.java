package com.sprint.deokhugam.book.adapter.in.web.dto;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "도서 수정 요청")
public record BookUpdateMultipartRequest(
    @Schema(
        description = "수정할 도서 정보",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    BookUpdateRequest bookData,

    @Schema(
        description = "변경할 도서 썸네일 이미지",
        type = "string",
        format = "binary"
    )
    MultipartFile thumbnailImage
) {
}
