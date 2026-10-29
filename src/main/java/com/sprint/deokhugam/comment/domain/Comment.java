package com.sprint.deokhugam.comment.domain;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.sprint.deokhugam.common.domain.BaseEntity;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.user.domain.User;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String content;

    @ColumnDefault("false")
    @Column(nullable = false)
    private boolean isDeleted;

    private Comment(Review review, User user, String content) {
        this.review = review;
        this.user = user;
        this.content = content;
    }

    public static Comment create(Review review, User user, String content) {
        return new Comment(review, user, ProfanityWord.mask(content));   // 변경
    }

    public void updateContent(String content) {
        this.content = ProfanityWord.mask(content);                      // 변경
    }

    public void softDelete() {
        this.isDeleted = true;
    }
}
