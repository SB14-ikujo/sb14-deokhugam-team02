package com.sprint.deokhugam.dashboard.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.sprint.deokhugam.review.domain.Review;

class PopularReviewRankerTest {
    private static final LocalDate BASE_DATE = LocalDate.of(2026, 10, 19);

    private final PopularReviewRanker popularReviewRanker = new PopularReviewRanker();

    @Test
    @DisplayName("점수가 높은 순으로 1위부터 순위를 매긴다")
    void rankByScoreDescending() {
        PopularReview reviewA = candidate("A", 1, 5.0, 3L);
        PopularReview reviewB = candidate("B", 2, 9.0, 3L);
        PopularReview reviewC = candidate("C", 3, 7.0, 3L);

        List<PopularReview> ranked = popularReviewRanker.rank(List.of(reviewA, reviewB, reviewC));

        assertThat(ranked)
            .extracting(popularReview -> popularReview.getReview().getContent(), PopularReview::getRank)
            .containsExactly(tuple("B", 1L), tuple("C", 2L), tuple("A", 3L));
    }

    @Test
    @DisplayName("점수가 같으면 댓글 수가 많은 리뷰가 앞선다")
    void rankByCommentCountWhenScoreTied() {
        PopularReview reviewA = candidate("A", 1, 5.0, 3L);
        PopularReview reviewB = candidate("B", 2, 5.0, 8L);

        List<PopularReview> ranked = popularReviewRanker.rank(List.of(reviewA, reviewB));

        assertThat(ranked)
            .extracting(popularReview -> popularReview.getReview().getContent(), PopularReview::getRank)
            .containsExactly(tuple("B", 1L), tuple("A", 2L));
    }

    @Test
    @DisplayName("점수와 댓글 수가 모두 같으면 리뷰 ID가 작은 리뷰가 앞선다")
    void rankByReviewIdWhenScoreAndCommentCountTied() {
        PopularReview reviewA = candidate("A", 2, 5.0, 3L);
        PopularReview reviewB = candidate("B", 1, 5.0, 3L);

        List<PopularReview> ranked = popularReviewRanker.rank(List.of(reviewA, reviewB));

        assertThat(ranked)
            .extracting(popularReview -> popularReview.getReview().getContent(), PopularReview::getRank)
            .containsExactly(tuple("B", 1L), tuple("A", 2L));
    }

    @Test
    @DisplayName("후보가 없으면 빈 목록을 돌려준다")
    void rankEmpty() {
        assertThat(popularReviewRanker.rank(List.of())).isEmpty();
    }

    private PopularReview candidate(String content, long idNumber, double score, long commentCount) {
        Review review = Review.create(null, null, content, 5);
        ReflectionTestUtils.setField(review, "id", new UUID(0L, idNumber));
        return PopularReview.create(review, PeriodType.DAILY, null, score, 1L, commentCount, BASE_DATE);
    }
}
