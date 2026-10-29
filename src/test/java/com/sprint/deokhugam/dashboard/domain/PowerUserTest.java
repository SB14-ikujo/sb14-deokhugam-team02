package com.sprint.deokhugam.dashboard.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.sprint.deokhugam.user.domain.User;

class PowerUserTest {
    private static final LocalDate BASE_DATE = LocalDate.of(2026, 10, 19);

    private User user;

    @BeforeEach
    void setUp() {
        user = User.create("user@test.com", "user", "password1!");
    }

    @ParameterizedTest(name = "리뷰 점수 합 {0}, 좋아요 {1}개, 댓글 {2}개 → 점수 {3}")
    @CsvSource({
        "4.0, 0,  0,  2.0",
        "0.0, 5,  0,  1.0",
        "0.0, 0,  10, 3.0",
        "2.0, 1,  2,  1.8"
    })
    @DisplayName("점수는 리뷰 점수 합 × 0.5 + 좋아요 수 × 0.2 + 댓글 수 × 0.3 이다")
    void candidateScore(double reviewScoreSum, long likeCount, long commentCount, double expectedScore) {
        PowerUser candidate = PowerUser.candidate(user, PeriodType.DAILY, reviewScoreSum, likeCount, commentCount,
            BASE_DATE);

        assertThat(candidate.getScore()).isCloseTo(expectedScore, within(0.000001));
    }

    @Test
    @DisplayName("후보는 순위 없이 리뷰 점수 합·좋아요 수·댓글 수를 제자리에 담는다")
    void candidateHasNoRank() {
        PowerUser candidate = PowerUser.candidate(user, PeriodType.WEEKLY, 4.0, 3L, 2L, BASE_DATE);

        assertThat(candidate.getRank()).isNull();
        assertThat(candidate.getUser()).isSameAs(user);
        assertThat(candidate.getPeriod()).isEqualTo(PeriodType.WEEKLY);
        assertThat(candidate.getReviewScoreSum()).isEqualTo(4.0);
        assertThat(candidate.getLikeCount()).isEqualTo(3L);
        assertThat(candidate.getCommentCount()).isEqualTo(2L);
        assertThat(candidate.getSnapshotDate()).isEqualTo(BASE_DATE);
    }

    @Test
    @DisplayName("후보에 순위를 배정한다")
    void assignRank() {
        PowerUser candidate = PowerUser.candidate(user, PeriodType.DAILY, 4.0, 3L, 2L, BASE_DATE);

        candidate.assignRank(1L);

        assertThat(candidate.getRank()).isEqualTo(1L);
    }

    @Test
    @DisplayName("이미 순위가 있으면 다시 배정할 수 없다")
    void assignRankTwiceThrows() {
        PowerUser candidate = PowerUser.candidate(user, PeriodType.DAILY, 4.0, 3L, 2L, BASE_DATE);
        candidate.assignRank(1L);

        assertThatThrownBy(() -> candidate.assignRank(2L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("순위는 한 번만 배정할 수 있습니다.");
    }

    @Test
    @DisplayName("순위는 1 이상이어야 한다")
    void assignRankBelowOneThrows() {
        PowerUser candidate = PowerUser.candidate(user, PeriodType.DAILY, 4.0, 3L, 2L, BASE_DATE);

        assertThatThrownBy(() -> candidate.assignRank(0L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("랭킹은 1 이상이어야 합니다.");
    }
}
