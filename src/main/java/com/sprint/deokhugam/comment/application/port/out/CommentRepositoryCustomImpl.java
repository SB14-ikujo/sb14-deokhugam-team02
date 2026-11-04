package com.sprint.deokhugam.comment.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.comment.domain.Comment;
import com.sprint.deokhugam.comment.domain.QComment;

@RequiredArgsConstructor
public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {

    private static final QComment comment = QComment.comment;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Comment> findByCursor(UUID reviewId, Instant after, UUID afterId,
        Sort.Direction direction, int size) {
        boolean asc = direction == Sort.Direction.ASC;

        return queryFactory
            .selectFrom(comment)
            .join(comment.user).fetchJoin()
            .where(
                comment.review.id.eq(reviewId),
                comment.isDeleted.isFalse(),
                cursorCondition(after, afterId, asc)
            )
            .orderBy(
                asc ? comment.createdAt.asc() : comment.createdAt.desc(),
                asc ? comment.id.asc() : comment.id.desc()
            )
            .limit(size)
            .fetch();
    }

    private BooleanExpression cursorCondition(Instant after, UUID afterId, boolean asc) {
        if (after == null || afterId == null) {
            return null;
        }
        if (asc) {
            return comment.createdAt.gt(after)
                .or(comment.createdAt.eq(after).and(comment.id.gt(afterId)));
        }
        return comment.createdAt.lt(after)
            .or(comment.createdAt.eq(after).and(comment.id.lt(afterId)));
    }
}
