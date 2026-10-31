package com.sprint.deokhugam.book.application.dto;

// 다운로드한 이미지의 바이트 데이터 보관
public record DownloadedThumbnail(
    byte[] bytes,
    String contentType,
    String originalFilename
) {
}
