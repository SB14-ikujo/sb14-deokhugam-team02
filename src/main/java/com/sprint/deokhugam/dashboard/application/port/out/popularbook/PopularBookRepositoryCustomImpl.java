package com.sprint.deokhugam.dashboard.application.port.out.popularbook;

import static com.sprint.deokhugam.book.domain.QBook.*;
import static com.sprint.deokhugam.dashboard.domain.QPopularBook.*;
import static com.sprint.deokhugam.review.domain.QReview.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.dashboard.domain.Direction;
import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PopularBook;

@RequiredArgsConstructor
public class PopularBookRepositoryCustomImpl implements PopularBookRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<LocalDate> findLatestSnapshotDate(PeriodType period) {
        LocalDate latest = jpaQueryFactory
            .select(popularBook.snapshotDate.max())
            .from(popularBook)
            .where(popularBook.period.eq(period))
            .fetchOne();
        return Optional.ofNullable(latest);
    }

    @Override
    public List<PopularBook> findPage(PeriodType period, LocalDate snapshotDate, Direction direction, Long cursorRank,
        int size) {

        return jpaQueryFactory.selectFrom(popularBook)
            .join(popularBook.book, book).fetchJoin()
            .where(
                popularBook.period.eq(period),
                popularBook.snapshotDate.eq(snapshotDate),
                rankAfter(direction, cursorRank)
            )
            .orderBy(direction == Direction.ASC ? popularBook.rank.asc() : popularBook.rank.desc())
            .limit(size)
            .fetch();
    }

    @Override
    public List<PopularBook> findCandidates(PeriodType period, LocalDate baseDate) {
        NumberExpression<Long> reviewCount = review.count();
        NumberExpression<Double> ratingAvg = review.rating.avg();

        List<Tuple> rows = jpaQueryFactory
            .select(book, reviewCount, ratingAvg)
            .from(review)
            .join(review.book, book)
            .where(
                createdAtFrom(period.startOf(baseDate)),    // 기준 시작 시각
                review.createdAt.lt(PeriodType.endOf(baseDate)) // 기준일 0시 전까지
            )
            .groupBy(book)
            .fetch();

        return rows.stream()
            .map(row -> PopularBook.candidate(row.get(book), period, row.get(reviewCount),
                Objects.requireNonNull(row.get(ratingAvg)), baseDate))
            .toList();
    }

    private BooleanExpression rankAfter(Direction direction, Long cursorRank) {
        if (cursorRank == null) {
            return null;
        }
        if (direction == Direction.ASC) {
            return popularBook.rank.gt(cursorRank);
        }
        return popularBook.rank.lt(cursorRank);
    }

    private BooleanExpression createdAtFrom(Instant start) {
        if (start == null) {
            return null;
        }
        return review.createdAt.goe(start);
    }
}
