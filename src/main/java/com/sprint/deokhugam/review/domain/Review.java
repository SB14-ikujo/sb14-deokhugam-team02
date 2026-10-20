package com.sprint.deokhugam.review.domain;

import java.util.UUID;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.common.domain.BaseEntity;
import com.sprint.deokhugam.review.domain.exception.ReviewErrorCode;
import com.sprint.deokhugam.review.domain.exception.ReviewException;
import com.sprint.deokhugam.user.domain.User;

@Entity
@Table(name = "reviews", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int rating;

    @ColumnDefault("0")
    @Column(nullable = false)
    private int likeCount;

    @ColumnDefault("0")
    @Column(nullable = false)
    private int commentCount;

    @ColumnDefault("false")
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    private Review(User user, Book book, String content, int rating) {
        this.user = user;
        this.book = book;
        this.content = content;
        this.rating = rating;
    }

    public static Review create(User user, Book book, String content, int rating) {
        return new Review(user, book, content, rating);
    }

    public void softDelete(UUID requestUserID) {
        // 이미 삭제된 리뷰는 소프트 딜리트를 할 수 없음
        if (this.isDeleted) {
            throw new ReviewException(ReviewErrorCode.REVIEW_ALREADY_DELETED);
        }
        // 해당 리뷰를 작성한 유저가 아니면 소프트 딜리트를 할 수 없음
        if (!requestUserID.equals(this.user.getId())) {
            throw new ReviewException(ReviewErrorCode.REVIEW_FORBIDDEN);
        }
        this.isDeleted = true;
    }

    public void update(String content, Integer rating) {
        // 1. 내용(content) 검증: null이거나 공백이면 예외를 확실히 던짐 (예외 삼키기 방지)
        if (content == null || content.isBlank()) {
            throw new ReviewException(ReviewErrorCode.INVALID_REVIEW_CONTENT);
        }

        // 2. 평점(rating) 검증: null이거나 범위를 벗어나면 예외를 확실히 던짐
        if (rating == null || rating < 1 || rating > 5) {
            throw new ReviewException(ReviewErrorCode.INVALID_REVIEW_RATING);
        }

        // 3. 완벽하게 검증된 안전한 데이터만 비즈니스 필드에 대입
        this.content = content;
        this.rating = rating;
    }

    // 리뷰 '좋아요 수' 상승
    public void increaseLikeCount() {
        this.likeCount += 1;
    }

    // 리뷰 '좋아요 수' 감소
    public void decreaseLikeCount() {
        this.likeCount -= 1;
    }

}
