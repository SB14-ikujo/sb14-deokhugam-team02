package com.sprint.deokhugam.dashboard.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.sprint.deokhugam.review.domain.Review;

class PopularReviewTest {
    private static final LocalDate BASE_DATE = LocalDate.of(2026, 10, 19);

    private Review review;

    @BeforeEach
    void setUp() {
        review = Review.create(null, null, "내용", 5);
    }

    @ParameterizedTest(name = "좋아요 {0}개, 댓글 {1}개 → 점수 {2}")
    @CsvSource({
        "3,  2, 2.3",
        "0,  1, 0.7",
        "10, 0, 3.0"
    })
    @DisplayName("점수는 좋아요 수 × 0.3 + 댓글 수 × 0.7 이다")
    void candidateScore(long likeCount, long commentCount, double expectedScore) {
        PopularReview candidate = PopularReview.candidate(review, PeriodType.DAILY, commentCount, likeCount,
            BASE_DATE);

        assertThat(candidate.getScore()).isCloseTo(expectedScore, within(0.000001));
    }

    @Test
    @DisplayName("후보는 순위 없이 좋아요 수와 댓글 수를 제자리에 담는다")
    void candidateHasNoRank() {
        PopularReview candidate = PopularReview.candidate(review, PeriodType.WEEKLY, 2L, 3L, BASE_DATE);

        assertThat(candidate.getRank()).isNull();
        assertThat(candidate.getReview()).isSameAs(review);
        assertThat(candidate.getPeriod()).isEqualTo(PeriodType.WEEKLY);
        assertThat(candidate.getCommentCount()).isEqualTo(2L);
        assertThat(candidate.getLikeCount()).isEqualTo(3L);
        assertThat(candidate.getSnapshotDate()).isEqualTo(BASE_DATE);
    }

    @Test
    @DisplayName("후보에 순위를 배정한다")
    void assignRank() {
        PopularReview candidate = PopularReview.candidate(review, PeriodType.DAILY, 2L, 3L, BASE_DATE);

        candidate.assignRank(1L);

        assertThat(candidate.getRank()).isEqualTo(1L);
    }

    @Test
    @DisplayName("이미 순위가 있으면 다시 배정할 수 없다")
    void assignRankTwiceThrows() {
        PopularReview candidate = PopularReview.candidate(review, PeriodType.DAILY, 2L, 3L, BASE_DATE);
        candidate.assignRank(1L);

        assertThatThrownBy(() -> candidate.assignRank(2L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("순위는 한 번만 배정할 수 있습니다.");
    }

    @Test
    @DisplayName("순위는 1 이상이어야 한다")
    void assignRankBelowOneThrows() {
        PopularReview candidate = PopularReview.candidate(review, PeriodType.DAILY, 2L, 3L, BASE_DATE);

        assertThatThrownBy(() -> candidate.assignRank(0L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("랭킹은 1 이상이어야 합니다.");
    }
}
