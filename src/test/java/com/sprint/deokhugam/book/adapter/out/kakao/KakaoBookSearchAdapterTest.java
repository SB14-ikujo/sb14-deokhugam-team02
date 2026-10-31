package com.sprint.deokhugam.book.adapter.out.kakao;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.sprint.deokhugam.book.application.dto.BookSearchResult;

class KakaoBookSearchAdapterTest {
    private MockRestServiceServer mockServer;
    private KakaoBookSearchAdapter kakaoBookSearchAdapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
            .baseUrl("https://dapi.kakao.com")
            .defaultHeader(
                "Authorization",
                "KakaoAk test-api-key"
            );

        mockServer = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder.build();

        kakaoBookSearchAdapter = new KakaoBookSearchAdapter(restClient);
    }

    @Test
    @DisplayName("ISBN으로 도서를 검색하고 내부 검색 결과 모델로 변환한다.")
    void searchByIsbnReturnsBookSearchResults() {
        String isbn = "9788966260959";

        String responseJson = """
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

        mockServer.expect(requestTo(
                "https://dapi.kakao.com/v3/search/book"
                    + "?query=9788966260959&target=isbn"
            ))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(
                responseJson,
                MediaType.APPLICATION_JSON
            ));

        List<BookSearchResult> results = kakaoBookSearchAdapter.searchByIsbn(isbn);

        assertThat(results).hasSize(1);

        BookSearchResult result = results.get(0);

        assertThat(result.title()).isEqualTo("클린 코드");
        assertThat(result.isbn()).isEqualTo(isbn);
        assertThat(result.publishedDate()).isEqualTo(LocalDate.of(2022, 5, 18));
        assertThat(result.authors()).containsExactly("로버트 C. 마틴");
        assertThat(result.publisher()).isEqualTo("인사이트");
        assertThat(result.price()).isEqualTo(30000);
        assertThat(result.salePrice()).isEqualTo(27000);

        mockServer.verify();
    }

    @Test
    @DisplayName("검색 결과가 없으면 빈 목록을 반환한다.")
    void searchByIsbnReturnsEmptyListWhenNoResults() {
        String responseJson = """
            {
                "meta": {
                    "total_count": 0,
                    "pageable_count": 0,
                    "is_end": true
                },
                "documents": []
            }
            """;

        mockServer.expect(requestTo(
                "https://dapi.kakao.com/v3/search/book"
                    + "?query=0000000000&target=isbn"
            ))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(
                responseJson,
                MediaType.APPLICATION_JSON
            ));

        List<BookSearchResult> results = kakaoBookSearchAdapter.searchByIsbn("0000000000");

        assertThat(results).isEmpty();

        mockServer.verify();
    }

    @Test
    @DisplayName("카카오 API가 오류 응답을 반환하면 예외가 발생한다.")
    void searchByIsbnThrowsExceptionWhenApiReturnsError() {
        mockServer.expect(requestTo(
                "https://dapi.kakao.com/v3/search/book"
                    + "?query=9788966260959&target=isbn"
            ))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withBadRequest());

        assertThatThrownBy(() -> kakaoBookSearchAdapter.searchByIsbn("9788966260959"))
            .isInstanceOf(RestClientResponseException.class);

        mockServer.verify();
    }

    @Test
    @DisplayName("응답 본문이 비어 있으면 빈 목록을 반환한다.")
    void searchByIsbnReturnsEmptyListWhenResponseBodyIsEmpty() {
        String isbn = "9788966260959";

        mockServer.expect(requestTo(
                "https://dapi.kakao.com/v3/search/book"
                    + "?query=" + isbn + "&target=isbn"
            ))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess());

        List<BookSearchResult> results =
            kakaoBookSearchAdapter.searchByIsbn(isbn);

        assertThat(results).isEmpty();

        mockServer.verify();
    }
}
