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
import com.sprint.deokhugam.review.domain.Review;

@Entity
@Table(name = "popular_reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularReview extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodType period;

    @Column(nullable = false)
    private long rank;

    @Column(nullable = false)
    private double score;

    @Column(nullable = false)
    private long likeCount;

    @Column(nullable = false)
    private long commentCount;

    public PopularReview(Review review, PeriodType period, long rank, double score,
        long likeCount, long commentCount) {
        this.review = review;
        this.period = period;
        this.rank = rank;
        this.score = score;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }
}
