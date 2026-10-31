package com.sprint.deokhugam.book.application.port.out;

import java.util.Optional;

import com.sprint.deokhugam.book.application.dto.DownloadedThumbnail;

// 카카오 썸네일 다운로드
public interface BookThumbnailDownloadPort {
    Optional<DownloadedThumbnail> download(String thumbnailUrl);
}
