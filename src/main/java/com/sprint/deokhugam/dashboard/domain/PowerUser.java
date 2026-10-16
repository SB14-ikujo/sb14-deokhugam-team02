package com.sprint.deokhugam.dashboard.domain;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
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

    private PowerUser(User user, PeriodType period, Long rank, double score,
        double reviewScoreSum, Long likeCount, Long commentCount) {
        this.user = user;
        this.period = period;
        this.rank = rank;
        this.score = score;
        this.reviewScoreSum = reviewScoreSum;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }

    public static PowerUser create(User user, PeriodType period, Long rank, double score,
        double reviewScoreSum, Long likeCount, Long commentCount) {

        return new PowerUser(user, period, rank, score, reviewScoreSum, likeCount, commentCount);
    }
}
