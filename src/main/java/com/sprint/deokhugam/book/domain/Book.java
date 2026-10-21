package com.sprint.deokhugam.book.domain;

import java.time.LocalDate;

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

import com.sprint.deokhugam.common.domain.BaseEntity;
import com.sprint.deokhugam.user.domain.User;

@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseEntity {

    @Column(nullable = false)
    private String title;   // 책제목

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String author;  // 저자

    @Column(nullable = false)
    private String description; // 설명

    @Column(nullable = false)
    private String publisher;   // 출판사

    @Column(nullable = false)
    private LocalDate publishedDate;   // 출판일

    @Column(unique = true, nullable = false)
    private String isbn;    // isbn 정보

    @Column(name = "thumbnail_url", nullable = true)
    private String thumbnailUrl;    // 썸네일 Url

    @ColumnDefault("0")
    @Column(nullable = false)
    private int reviewCount = 0;    // 리뷰 수

    @ColumnDefault("0")
    @Column(nullable = false)
    private double rating = 0.0;  // 점수

    @ColumnDefault("false")
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    private Book(String title, User user, String author, String description, String publisher,
        LocalDate publishedDate, String isbn, String thumbnailUrl) {
        this.title = title;
        this.user = user;
        this.author = author;
        this.description = description;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.isbn = isbn;
        this.thumbnailUrl = thumbnailUrl;
    }

    public static Book create(String title, User user, String author, String description, String publisher,
        LocalDate publishedDate, String isbn, String thumbnailUrl) {
        return new Book(title, user, author, description, publisher, publishedDate, isbn, thumbnailUrl);
    }

}
