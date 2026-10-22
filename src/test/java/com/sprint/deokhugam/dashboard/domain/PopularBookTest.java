package com.sprint.deokhugam.dashboard.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.sprint.deokhugam.book.domain.Book;

class PopularBookTest {
    private static final LocalDate BASE_DATE = LocalDate.of(2026, 10, 19);

    private Book book;

    @BeforeEach
    void setUp() {
        book = Book.create("제목", "저자", "설명", "출판사", LocalDate.of(2020, 1, 1), "9870000000001", null);
    }

    @ParameterizedTest(name = "리뷰 {0}개, 평점 {1} → 점수 {2}")
    @CsvSource({
        "10, 4.5, 6.7",
        "1,  5.0, 3.4",
        "3,  2.0, 2.4"
    })
    @DisplayName("점수는 리뷰 수 × 0.4 + 평점 평균 × 0.6 이다")
    void candidateScore(long reviewCount, double rating, double expectedScore) {
        PopularBook candidate = PopularBook.candidate(book, PeriodType.DAILY, reviewCount, rating, BASE_DATE);

        assertThat(candidate.getScore()).isCloseTo(expectedScore, within(0.000001));
    }

    @Test
    @DisplayName("후보는 순위 없이 집계 값을 그대로 담는다")
    void candidateHasNoRank() {
        PopularBook candidate = PopularBook.candidate(book, PeriodType.WEEKLY, 3L, 4.0, BASE_DATE);

        assertThat(candidate.getRank()).isNull();
        assertThat(candidate.getBook()).isSameAs(book);
        assertThat(candidate.getPeriod()).isEqualTo(PeriodType.WEEKLY);
        assertThat(candidate.getReviewCount()).isEqualTo(3L);
        assertThat(candidate.getRating()).isEqualTo(4.0);
        assertThat(candidate.getSnapshotDate()).isEqualTo(BASE_DATE);
    }

    @Test
    @DisplayName("후보에 순위를 배정한다")
    void assignRank() {
        PopularBook candidate = PopularBook.candidate(book, PeriodType.DAILY, 3L, 4.0, BASE_DATE);

        candidate.assignRank(1L);

        assertThat(candidate.getRank()).isEqualTo(1L);
    }

    @Test
    @DisplayName("이미 순위가 있으면 다시 배정할 수 없다")
    void assignRankTwiceThrows() {
        PopularBook candidate = PopularBook.candidate(book, PeriodType.DAILY, 3L, 4.0, BASE_DATE);
        candidate.assignRank(1L);

        assertThatThrownBy(() -> candidate.assignRank(2L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("순위는 한 번만 배정할 수 있습니다.");
    }

    @Test
    @DisplayName("순위는 1 이상이어야 한다")
    void assignRankBelowOneThrows() {
        PopularBook candidate = PopularBook.candidate(book, PeriodType.DAILY, 3L, 4.0, BASE_DATE);

        assertThatThrownBy(() -> candidate.assignRank(0L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("순위는 1 이상이어야 합니다.");
    }
}
