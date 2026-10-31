package com.sprint.deokhugam.book.adapter.out.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * KakaoBookMeta : 검색 결과 개수와 마지막 페이지 여부
 */
public record KakaoBookMeta(
    @JsonProperty("total_count") int totalCount,
    @JsonProperty("pageable_count") int pageableCount,
    @JsonProperty("is_end") boolean isEnd
) {
}
