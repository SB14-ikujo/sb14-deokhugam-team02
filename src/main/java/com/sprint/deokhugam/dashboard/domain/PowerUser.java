package com.sprint.deokhugam.dashboard.domain;

import java.time.LocalDate;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.sprint.deokhugam.common.domain.BaseEntity;
import com.sprint.deokhugam.user.domain.User;

@Entity
@Getter
@Table(name = "power_users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PowerUser extends BaseEntity {

    private static final double REVIEW_SCORE_SUM_WEIGHT = 0.5;
    private static final double LIKE_COUNT_WEIGHT = 0.2;
    private static final double COMMENT_COUNT_WEIGHT = 0.3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodType period;

    @Column(nullable = false)
    private Long rank;

    @Column(nullable = false)
    private double score;

    @Column(nullable = false)
    private double reviewScoreSum;

    @Column(nullable = false)
    private Long likeCount;

    @Column(nullable = false)
    private Long commentCount;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    private PowerUser(User user, PeriodType period, Long rank, double score,
        double reviewScoreSum, Long likeCount, Long commentCount, LocalDate snapshotDate) {
        this.user = user;
        this.period = period;
        this.rank = rank;
        this.score = score;
        this.reviewScoreSum = reviewScoreSum;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.snapshotDate = snapshotDate;
    }

    public static PowerUser create(User user, PeriodType period, Long rank, double score,
        double reviewScoreSum, Long likeCount, Long commentCount, LocalDate snapshotDate) {

        return new PowerUser(user, period, rank, score, reviewScoreSum, likeCount, commentCount, snapshotDate);
    }

    public static PowerUser candidate(User user, PeriodType period, double reviewScoreSum,
        Long likeCount, Long commentCount, LocalDate snapshotDate) {

        double score = reviewScoreSum * REVIEW_SCORE_SUM_WEIGHT + likeCount * LIKE_COUNT_WEIGHT
            + commentCount * COMMENT_COUNT_WEIGHT;
        return new PowerUser(user, period, null, score, reviewScoreSum, likeCount, commentCount, snapshotDate);
    }

    public void assignRank(long rank) {
        if (this.rank != null) {
            throw new IllegalStateException("순위는 한 번만 배정할 수 있습니다.");
        }
        if (rank < 1) {
            throw new IllegalStateException("랭킹은 1 이상이어야 합니다.");
        }

        this.rank = rank;
    }
}
