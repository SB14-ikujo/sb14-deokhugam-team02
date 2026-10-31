package com.sprint.deokhugam.book.adapter.out.kakao.dto;

import java.util.List;

public record KakaoBookSearchResponse(
    KakaoBookMeta meta,
    List<KakaoBookDocument> documents
) {
}
