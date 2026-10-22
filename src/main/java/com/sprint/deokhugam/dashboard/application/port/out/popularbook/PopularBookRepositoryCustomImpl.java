package com.sprint.deokhugam.dashboard.application.port.out.popularbook;

import static com.sprint.deokhugam.book.domain.QBook.*;
import static com.sprint.deokhugam.dashboard.domain.QPopularBook.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
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
        // TODO: 리뷰 집계 쿼리로 구현 -> 곧 할거임
        return List.of();
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
}
