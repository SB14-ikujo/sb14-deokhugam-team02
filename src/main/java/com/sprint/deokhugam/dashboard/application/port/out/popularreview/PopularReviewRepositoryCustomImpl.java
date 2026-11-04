package com.sprint.deokhugam.dashboard.application.port.out.popularreview;

import static com.sprint.deokhugam.book.domain.QBook.*;
import static com.sprint.deokhugam.comment.domain.QComment.*;
import static com.sprint.deokhugam.dashboard.domain.QPopularReview.*;
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
import com.sprint.deokhugam.dashboard.domain.PopularReview;
import com.sprint.deokhugam.review.domain.Review;

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

    @Override
    public List<PopularReview> findCandidates(PeriodType period, LocalDate baseDate) {
        NumberExpression<Long> likeCount = reviewLike.count();
        NumberExpression<Long> commentCount = comment.count();

        List<Tuple> likeRows = jpaQueryFactory
            .select(review, likeCount)
            .from(reviewLike)
            .join(reviewLike.review, review)
            .where(
                likedAtFrom(period.startOf(baseDate)),
                reviewLike.createdAt.lt(PeriodType.endOf(baseDate))
            )
            .groupBy(review)
            .fetch();

        List<Tuple> commentRows = jpaQueryFactory
            .select(review, commentCount)
            .from(comment)
            .join(comment.review, review)
            .where(
                commentAtFrom(period.startOf(baseDate)),
                comment.createdAt.lt(PeriodType.endOf(baseDate))
            )
            .groupBy(review)
            .fetch();

        // 리뷰 id별로 좋아요 수를 모은다
        Map<UUID, Review> reviewById = new HashMap<>();
        Map<UUID, Long> likeCountByReviewId = new HashMap<>();
        for (Tuple row : likeRows) {
            Review found = row.get(review);
            reviewById.put(found.getId(), found);
            likeCountByReviewId.put(found.getId(), row.get(likeCount));
        }

        // 리뷰 id별로 댓글 수를 모은다
        Map<UUID, Long> commentCountByReviewId = new HashMap<>();
        for (Tuple row : commentRows) {
            Review found = row.get(review);
            reviewById.put(found.getId(), found);
            commentCountByReviewId.put(found.getId(), row.get(commentCount));
        }

        // 한쪽에만 있는 리뷰는 없는 쪽을 0으로 채워 후보를 만든다
        List<PopularReview> candidates = new ArrayList<>();
        for (Review target : reviewById.values()) {
            Long likes = likeCountByReviewId.getOrDefault(target.getId(), 0L);
            Long comments = commentCountByReviewId.getOrDefault(target.getId(), 0L);
            candidates.add(PopularReview.candidate(target, period, comments, likes, baseDate));
        }
        return candidates;
    }

    @Override
    public List<PopularReview> findTop10ByPeriod(PeriodType period, LocalDate baseDate) {
        return jpaQueryFactory.selectFrom(popularReview)
            .join(popularReview.review, review).fetchJoin()
            .join(review.user, user).fetchJoin()
            .where(
                popularReview.period.eq(period),
                popularReview.snapshotDate.eq(baseDate)
            )
            .orderBy(popularReview.rank.asc())
            .limit(10)
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
