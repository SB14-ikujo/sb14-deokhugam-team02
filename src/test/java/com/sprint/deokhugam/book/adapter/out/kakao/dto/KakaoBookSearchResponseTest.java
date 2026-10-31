package com.sprint.deokhugam.book.adapter.out.kakao.dto;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class KakaoBookSearchResponseTest {
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    @DisplayName("카카오 도서 검색 응답을 역직렬화한다.")
    void deserializeBookSearchResponse() throws Exception {
        // given
        String json = """
            {
                "meta": {
                    "total_count": 1,
                    "pageable_count": 1,
                    "is_end": true
                },
                "documents": [
                    {
                        "title": "클린 코드",
                        "contents": "좋은 코드를 작성하기 위한 원칙을 설명하는 책",
                        "url": "https://example.com/book",
                        "isbn": "9788966260959",
                        "datetime": "2022-05-18T00:00:00.000+09:00",
                        "authors": ["로버트 C. 마틴"],
                        "publisher": "인사이트",
                        "translators": ["박재호"],
                        "price": 30000,
                        "sale_price": 27000,
                        "thumbnail": "https://example.com/book.jpg",
                        "status": "정상판매"
                    }
                ]
            }
            """;

        // when
        KakaoBookSearchResponse response = objectMapper.readValue(json, KakaoBookSearchResponse.class);

        // then
        assertThat(response.meta().totalCount()).isEqualTo(1);
        assertThat(response.meta().pageableCount()).isEqualTo(1);
        assertThat(response.meta().isEnd()).isTrue();

        assertThat(response.documents()).hasSize(1);

        KakaoBookDocument document = response.documents().get(0);

        assertThat(document.title()).isEqualTo("클린 코드");
        assertThat(document.isbn()).isEqualTo("9788966260959");
        assertThat(document.authors()).containsExactly("로버트 C. 마틴");
        assertThat(document.publisher()).isEqualTo("인사이트");
        assertThat(document.price()).isEqualTo(30000);
        assertThat(document.salePrice()).isEqualTo(27000);
        assertThat(document.thumbnail()).isEqualTo("https://example.com/book.jpg");
        assertThat(document.datetime()).isNotNull();
    }

    @Test
    @DisplayName("검색 결과가 없으면 빈 도서 목록을 반환한다")
    void returnEmptyDocumentsWhenNoSearchResults() throws Exception {
        // given
        String json = """
            {
              "meta": {
                "total_count": 0,
                "pageable_count": 0,
                "is_end": true
              },
              "documents": []
            }
            """;

        // when
        KakaoBookSearchResponse response =
            objectMapper.readValue(json, KakaoBookSearchResponse.class);

        // then
        assertThat(response.meta().totalCount()).isZero();
        assertThat(response.documents()).isEmpty();
    }
}
