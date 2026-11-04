package com.sprint.deokhugam.dashboard.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.sprint.deokhugam.user.domain.User;

class PowerUserRankerTest {
    private static final LocalDate BASE_DATE = LocalDate.of(2026, 10, 19);

    private final PowerUserRanker powerUserRanker = new PowerUserRanker();

    @Test
    @DisplayName("점수가 높은 순으로 1위부터 순위를 매긴다")
    void rankByScoreDescending() {
        PowerUser userA = candidate("userA", 1, 5.0, 0.0, 0L, 0L);
        PowerUser userB = candidate("userB", 2, 9.0, 0.0, 0L, 0L);
        PowerUser userC = candidate("userC", 3, 7.0, 0.0, 0L, 0L);

        List<PowerUser> ranked = powerUserRanker.rank(List.of(userA, userB, userC));

        assertThat(ranked)
            .extracting(powerUser -> powerUser.getUser().getNickname(), PowerUser::getRank)
            .containsExactly(tuple("userB", 1L), tuple("userC", 2L), tuple("userA", 3L));
    }

    @Test
    @DisplayName("점수가 같으면 리뷰 점수 합이 큰 사용자가 앞선다")
    void rankByReviewScoreSumWhenScoreTied() {
        PowerUser userA = candidate("userA", 1, 2.0, 0.0, 10L, 0L);
        PowerUser userB = candidate("userB", 2, 2.0, 4.0, 0L, 0L);

        List<PowerUser> ranked = powerUserRanker.rank(List.of(userA, userB));

        assertThat(ranked)
            .extracting(powerUser -> powerUser.getUser().getNickname(), PowerUser::getRank)
            .containsExactly(tuple("userB", 1L), tuple("userA", 2L));
    }

    @Test
    @DisplayName("점수와 리뷰 점수 합이 같으면 좋아요 수가 많은 사용자가 앞선다")
    void rankByLikeCountWhenScoreAndReviewScoreSumTied() {
        PowerUser userA = candidate("userA", 1, 2.0, 1.0, 1L, 9L);
        PowerUser userB = candidate("userB", 2, 2.0, 1.0, 5L, 1L);

        List<PowerUser> ranked = powerUserRanker.rank(List.of(userA, userB));

        assertThat(ranked)
            .extracting(powerUser -> powerUser.getUser().getNickname(), PowerUser::getRank)
            .containsExactly(tuple("userB", 1L), tuple("userA", 2L));
    }

    @Test
    @DisplayName("점수·리뷰 점수 합·좋아요 수가 같으면 댓글 수가 많은 사용자가 앞선다")
    void rankByCommentCountWhenLikeCountTied() {
        PowerUser userA = candidate("userA", 1, 2.0, 1.0, 1L, 1L);
        PowerUser userB = candidate("userB", 2, 2.0, 1.0, 1L, 3L);

        List<PowerUser> ranked = powerUserRanker.rank(List.of(userA, userB));

        assertThat(ranked)
            .extracting(powerUser -> powerUser.getUser().getNickname(), PowerUser::getRank)
            .containsExactly(tuple("userB", 1L), tuple("userA", 2L));
    }

    @Test
    @DisplayName("모든 기준이 같으면 사용자 ID가 작은 사용자가 앞선다")
    void rankByUserIdWhenAllTied() {
        PowerUser userA = candidate("userA", 2, 2.0, 1.0, 1L, 1L);
        PowerUser userB = candidate("userB", 1, 2.0, 1.0, 1L, 1L);

        List<PowerUser> ranked = powerUserRanker.rank(List.of(userA, userB));

        assertThat(ranked)
            .extracting(powerUser -> powerUser.getUser().getNickname(), PowerUser::getRank)
            .containsExactly(tuple("userB", 1L), tuple("userA", 2L));
    }

    @Test
    @DisplayName("후보가 없으면 빈 목록을 돌려준다")
    void rankEmpty() {
        assertThat(powerUserRanker.rank(List.of())).isEmpty();
    }

    private PowerUser candidate(String nickname, long idNumber, double score, double reviewScoreSum,
        long likeCount, long commentCount) {
        User user = User.create(nickname + "@test.com", nickname, "password1!");
        ReflectionTestUtils.setField(user, "id", new UUID(0L, idNumber));
        return PowerUser.create(user, PeriodType.DAILY, null, score, reviewScoreSum, likeCount, commentCount,
            BASE_DATE);
    }
}
