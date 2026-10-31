package com.sprint.deokhugam.review.domain;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.sprint.deokhugam.common.domain.BaseEntity;
import com.sprint.deokhugam.user.domain.User;

@Entity
@Getter
@Table(
    name = "review_likes",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_review_user_like", columnNames = {"review_id", "user_id"})
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewLike extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    private ReviewLike(Review review, User user) {
        this.review = review;
        this.user = user;
    }

    public static ReviewLike create(Review review, User user) {
        return new ReviewLike(review, user);
    }

}
