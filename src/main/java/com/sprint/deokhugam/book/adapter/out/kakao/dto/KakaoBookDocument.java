package com.sprint.deokhugam.book.adapter.out.kakao.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * KakaoBookDocument : 검색된 도서 목록
 */
public record KakaoBookDocument(
    String title,
    String contents,
    String url,
    String isbn,
    String datetime,
    List<String> authors,
    String publisher,
    List<String> translators,
    Integer price,

    @JsonProperty("sale_price")
    Integer salePrice,

    String thumbnail,
    String status
) {
}
