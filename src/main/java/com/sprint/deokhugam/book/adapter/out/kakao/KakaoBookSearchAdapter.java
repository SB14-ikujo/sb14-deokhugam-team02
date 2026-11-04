package com.sprint.deokhugam.book.adapter.out.kakao;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.sprint.deokhugam.book.adapter.out.kakao.dto.KakaoBookDocument;
import com.sprint.deokhugam.book.adapter.out.kakao.dto.KakaoBookSearchResponse;
import com.sprint.deokhugam.book.application.dto.BookSearchResult;
import com.sprint.deokhugam.book.application.port.out.BookSearchPort;

@Component
public class KakaoBookSearchAdapter implements BookSearchPort {
    private final RestClient kakaoBookRestClient;

    public KakaoBookSearchAdapter(
        @Qualifier("kakaoBookRestClient") RestClient kakaoBookRestClient
    ) {
        this.kakaoBookRestClient = kakaoBookRestClient;
    }

    @Override
    public List<BookSearchResult> searchByIsbn(String isbn) {
        KakaoBookSearchResponse response = kakaoBookRestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v3/search/book")
                .queryParam("query", isbn)                  // ISBN을 검색어로 전달
                .queryParam("target", "isbn")       // ISBN 기준으로 검색하도록 지정
                .build())
            .retrieve()
            .body(KakaoBookSearchResponse.class);

        if (response == null || response.documents() == null) {
            return List.of();
        }

        return response.documents().stream()
            .map(this::toBookSearchResult)
            .toList();
    }

    // 카카오 응답 DTO를 내부 검색 결과용으로 변환
    private BookSearchResult toBookSearchResult(KakaoBookDocument document) {
        return new BookSearchResult(
            document.title(),
            document.contents(),
            document.url(),
            document.isbn(),
            document.datetime() == null || document.datetime().isBlank()
                ? null : OffsetDateTime.parse(document.datetime()).toLocalDate(),
            document.authors(),
            document.publisher(),
            document.translators(),
            document.price(),
            document.salePrice(),
            document.thumbnail(),
            document.status()
        );
    }
}
