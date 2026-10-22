package com.sprint.deokhugam.dashboard.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.sprint.deokhugam.book.domain.Book;

class PopularBookRankerTest {
    private static final LocalDate BASE_DATE = LocalDate.of(2026, 10, 19);

    private final PopularBookRanker popularBookRanker = new PopularBookRanker();

    @Test
    @DisplayName("점수가 높은 순으로 1위부터 순위를 매긴다")
    void rankByScoreDescending() {
        PopularBook bookA = candidate("A", 1, 5.0, 3L);
        PopularBook bookB = candidate("B", 2, 9.0, 3L);
        PopularBook bookC = candidate("C", 3, 7.0, 3L);

        List<PopularBook> ranked = popularBookRanker.rank(List.of(bookA, bookB, bookC));

        assertThat(ranked)
            .extracting(popularBook -> popularBook.getBook().getTitle(), PopularBook::getRank)
            .containsExactly(tuple("B", 1L), tuple("C", 2L), tuple("A", 3L));
    }

    @Test
    @DisplayName("점수가 같으면 리뷰 수가 많은 도서가 앞선다")
    void rankByReviewCountWhenScoreTied() {
        PopularBook bookA = candidate("A", 1, 5.0, 3L);
        PopularBook bookB = candidate("B", 2, 5.0, 8L);

        List<PopularBook> ranked = popularBookRanker.rank(List.of(bookA, bookB));

        assertThat(ranked)
            .extracting(popularBook -> popularBook.getBook().getTitle(), PopularBook::getRank)
            .containsExactly(tuple("B", 1L), tuple("A", 2L));
    }

    @Test
    @DisplayName("점수와 리뷰 수가 모두 같으면 도서 ID가 작은 도서가 앞선다")
    void rankByBookIdWhenScoreAndReviewCountTied() {
        PopularBook bookA = candidate("A", 2, 5.0, 3L);
        PopularBook bookB = candidate("B", 1, 5.0, 3L);

        List<PopularBook> ranked = popularBookRanker.rank(List.of(bookA, bookB));

        assertThat(ranked)
            .extracting(popularBook -> popularBook.getBook().getTitle(), PopularBook::getRank)
            .containsExactly(tuple("B", 1L), tuple("A", 2L));
    }

    @Test
    @DisplayName("후보가 없으면 빈 목록을 돌려준다")
    void rankEmpty() {
        assertThat(popularBookRanker.rank(List.of())).isEmpty();
    }

    private PopularBook candidate(String title, long idNumber, double score, long reviewCount) {
        Book book = Book.create(title, "저자", "설명", "출판사", LocalDate.of(2020, 1, 1), "isbn-" + title, null);
        ReflectionTestUtils.setField(book, "id", new UUID(0L, idNumber));
        return PopularBook.create(book, PeriodType.DAILY, null, score, reviewCount, 4.0, BASE_DATE);
    }
}
