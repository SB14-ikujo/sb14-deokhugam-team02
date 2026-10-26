package com.sprint.deokhugam.dashboard.domain;

import java.time.LocalDate;

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
import com.sprint.deokhugam.review.domain.Review;

@Entity
@Table(name = "popular_reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularReview extends BaseEntity {

    private static final double LIKE_COUNT_WEIGHT = 0.3;
    private static final double COMMENT_COUNT_WEIGHT = 0.7;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodType period;

    @Column(nullable = false)
    private Long rank;

    @Column(nullable = false)
    private double score;

    @Column(nullable = false)
    private Long likeCount;

    @Column(nullable = false)
    private Long commentCount;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    private PopularReview(Review review, PeriodType period, Long rank, double score,
        Long likeCount, Long commentCount, LocalDate snapshotDate) {
        this.review = review;
        this.period = period;
        this.rank = rank;
        this.score = score;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.snapshotDate = snapshotDate;
    }

    public static PopularReview create(Review review, PeriodType period, Long rank, double score,
        Long likeCount, Long commentCount, LocalDate snapshotDate) {

        return new PopularReview(review, period, rank, score, likeCount, commentCount, snapshotDate);
    }

    public static PopularReview candidate(Review review, PeriodType period, Long commentCount,
        Long likeCount, LocalDate snapshotDate) {

        double score = commentCount * COMMENT_COUNT_WEIGHT + likeCount * LIKE_COUNT_WEIGHT;
        return new PopularReview(review, period, null, score, likeCount, commentCount, snapshotDate);
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
