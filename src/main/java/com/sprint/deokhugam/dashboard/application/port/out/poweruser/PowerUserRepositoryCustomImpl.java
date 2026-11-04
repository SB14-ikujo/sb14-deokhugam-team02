package com.sprint.deokhugam.dashboard.application.port.out.poweruser;

import static com.sprint.deokhugam.comment.domain.QComment.*;
import static com.sprint.deokhugam.dashboard.domain.QPopularReview.*;
import static com.sprint.deokhugam.dashboard.domain.QPowerUser.*;
import static com.sprint.deokhugam.review.domain.QReview.*;
import static com.sprint.deokhugam.review.domain.QReviewLike.*;
import static com.sprint.deokhugam.user.domain.QUser.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.dashboard.domain.Direction;
import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PowerUser;
import com.sprint.deokhugam.user.domain.User;

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

    @Override
    public List<PowerUser> findCandidates(PeriodType period, LocalDate snapshotDate) {
        NumberExpression<Double> reviewScoreSum = popularReview.score.sumDouble();
        NumberExpression<Long> likeCount = reviewLike.count();
        NumberExpression<Long> commentCount = comment.count();

        List<Tuple> scoreRows = jpaQueryFactory
            .select(user, reviewScoreSum)
            .from(popularReview)
            .join(popularReview.review, review)
            .join(review.user, user)
            .where(
                popularReview.period.eq(period),
                popularReview.snapshotDate.eq(snapshotDate)
            )
            .groupBy(user)
            .fetch();

        List<Tuple> likeRows = jpaQueryFactory
            .select(user, likeCount)
            .from(reviewLike)
            .join(reviewLike.user, user)
            .where(
                likedAtFrom(period.startOf(snapshotDate)),
                reviewLike.createdAt.lt(PeriodType.endOf(snapshotDate))
            )
            .groupBy(user)
            .fetch();

        List<Tuple> commentRows = jpaQueryFactory
            .select(user, commentCount)
            .from(comment)
            .join(comment.user, user)
            .where(
                commentAtFrom(period.startOf(snapshotDate)),
                comment.createdAt.lt(PeriodType.endOf(snapshotDate))
            )
            .groupBy(user)
            .fetch();

        Map<UUID, User> userById = new HashMap<>();
        Map<UUID, Double> scoreByUserId = new HashMap<>();
        for (Tuple row : scoreRows) {
            User found = row.get(user);
            userById.put(found.getId(), found);
            scoreByUserId.put(found.getId(), row.get(reviewScoreSum));
        }

        Map<UUID, Long> likeByUserId = new HashMap<>();
        for (Tuple row : likeRows) {
            User found = row.get(user);
            userById.put(found.getId(), found);
            likeByUserId.put(found.getId(), row.get(likeCount));
        }

        Map<UUID, Long> commentByUserId = new HashMap<>();
        for (Tuple row : commentRows) {
            User found = row.get(user);
            userById.put(found.getId(), found);
            commentByUserId.put(found.getId(), row.get(commentCount));
        }

        List<PowerUser> candidates = new ArrayList<>();
        for (User target : userById.values()) {
            double scoreSum = scoreByUserId.getOrDefault(target.getId(), 0.0);
            long likes = likeByUserId.getOrDefault(target.getId(), 0L);
            long comments = commentByUserId.getOrDefault(target.getId(), 0L);
            candidates.add(PowerUser.candidate(target, period, scoreSum, likes, comments, snapshotDate));
        }

        return candidates;
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

    private BooleanExpression likedAtFrom(Instant start) {
        if (start == null) {
            return null;
        }
        return reviewLike.createdAt.goe(start);
    }

    private BooleanExpression commentAtFrom(Instant start) {
        if (start == null) {
            return null;
        }
        return comment.createdAt.goe(start);
    }
}
