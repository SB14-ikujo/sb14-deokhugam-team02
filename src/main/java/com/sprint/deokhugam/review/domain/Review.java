package com.sprint.deokhugam.review.domain;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.common.domain.BaseEntity;
import com.sprint.deokhugam.user.domain.User;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
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

}
