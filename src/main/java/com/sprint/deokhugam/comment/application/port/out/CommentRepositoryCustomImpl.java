package com.sprint.deokhugam.comment.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
    public List<Comment> findByCursor(UUID reviewId, Instant after, UUID afterId, int size) {
        return queryFactory
            .selectFrom(comment)
            .join(comment.user).fetchJoin()
            .where(
                comment.review.id.eq(reviewId),
                comment.isDeleted.isFalse(),
                cursorCondition(after, afterId)
            )
            .orderBy(comment.createdAt.desc(), comment.id.desc())
            .limit(size)
            .fetch();
    }

    // 커서가 없으면 null을 반환 → where()가 null 조건을 무시하므로 첫 페이지 조회가 됨
    private BooleanExpression cursorCondition(Instant after, UUID afterId) {
        if (after == null || afterId == null) {
            return null;
        }
        return comment.createdAt.lt(after)
            .or(comment.createdAt.eq(after).and(comment.id.lt(afterId)));
    }
}
