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

import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.common.domain.BaseEntity;

@Entity
@Table(name = "popular_books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularBook extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PeriodType period;

    @Column(nullable = false)
    private Long rank;

    @Column(nullable = false)
    private double score;

    @Column(nullable = false)
    private Long reviewCount;

    @Column(nullable = false)
    private double rating;

    public PopularBook(Book book, PeriodType period, Long rank, double score,
        Long reviewCount, double rating) {
        this.book = book;
        this.period = period;
        this.rank = rank;
        this.score = score;
        this.reviewCount = reviewCount;
        this.rating = rating;
    }
}
