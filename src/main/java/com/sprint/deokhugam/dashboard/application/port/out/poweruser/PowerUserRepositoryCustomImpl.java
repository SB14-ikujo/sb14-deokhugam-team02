package com.sprint.deokhugam.dashboard.application.port.out.poweruser;

import static com.sprint.deokhugam.dashboard.domain.QPowerUser.*;
import static com.sprint.deokhugam.user.domain.QUser.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.dashboard.domain.Direction;
import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PowerUser;

@RequiredArgsConstructor
public class PowerUserRepositoryCustomImpl implements PowerUserRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<LocalDate> findLatestSnapshotDate(PeriodType period) {

        LocalDate localDate = jpaQueryFactory
            .select(powerUser.snapshotDate.max())
            .from(powerUser)
            .where(powerUser.period.eq(period))
            .fetchOne();

        return Optional.ofNullable(localDate);
    }

    @Override
    public List<PowerUser> findPage(PeriodType period, LocalDate snapshotDate, Direction direction, Long cursorRank,
        int size) {
        return jpaQueryFactory
            .selectFrom(powerUser)
            .join(powerUser.user, user).fetchJoin()
            .where(
                powerUser.period.eq(period),
                powerUser.snapshotDate.eq(snapshotDate),
                afterRank(direction, cursorRank)
            )
            .orderBy(direction == Direction.ASC ? powerUser.rank.asc() : powerUser.rank.desc())
            .limit(size)
            .fetch();
    }

    private BooleanExpression afterRank(Direction direction, Long cursorRank) {
        if (cursorRank == null) {
            return null;
        }
        if (direction == Direction.ASC) {
            return powerUser.rank.gt(cursorRank);
        }
        return powerUser.rank.lt(cursorRank);
    }
}
