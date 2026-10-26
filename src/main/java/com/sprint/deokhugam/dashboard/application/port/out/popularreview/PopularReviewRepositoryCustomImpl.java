package com.sprint.deokhugam.dashboard.application.port.out.popularreview;

import static com.sprint.deokhugam.book.domain.QBook.*;
import static com.sprint.deokhugam.dashboard.domain.QPopularReview.*;
import static com.sprint.deokhugam.review.domain.QReview.*;
import static com.sprint.deokhugam.user.domain.QUser.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.dashboard.domain.Direction;
import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PopularReview;

@RequiredArgsConstructor
public class PopularReviewRepositoryCustomImpl implements PopularReviewRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<LocalDate> findLatestSnapshotDate(PeriodType period) {

        LocalDate date = jpaQueryFactory
            .select(popularReview.snapshotDate.max())
            .from(popularReview)
            .where(popularReview.period.eq(period))
            .fetchOne();

        return Optional.ofNullable(date);
    }

    @Override
    public List<PopularReview> findPage(PeriodType period, LocalDate snapshotDate, Direction direction, Long cursorRank,
        int size) {
        return jpaQueryFactory.selectFrom(popularReview)
            .join(popularReview.review, review).fetchJoin()
            .join(review.book, book).fetchJoin()
            .join(review.user, user).fetchJoin()
            .where(
                popularReview.period.eq(period),
                popularReview.snapshotDate.eq(snapshotDate),
                afterRank(direction, cursorRank)
            )
            .orderBy(direction == Direction.ASC ? popularReview.rank.asc() : popularReview.rank.desc())
            .limit(size)
            .fetch();
    }

    private BooleanExpression afterRank(Direction direction, Long cursorRank) {
        if (cursorRank == null) {
            return null;
        }
        if (direction == Direction.ASC) {
            return popularReview.rank.gt(cursorRank);
        }
        return popularReview.rank.lt(cursorRank);
    }
}
