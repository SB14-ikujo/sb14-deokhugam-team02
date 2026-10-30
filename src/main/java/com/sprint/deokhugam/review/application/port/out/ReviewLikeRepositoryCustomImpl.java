package com.sprint.deokhugam.review.application.port.out;

import java.util.List;
import java.util.UUID;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.review.domain.QReviewLike;

@RequiredArgsConstructor
public class ReviewLikeRepositoryCustomImpl implements ReviewLikeRepositoryCustom {

    private static final QReviewLike reviewLike = QReviewLike.reviewLike;
    private final JPAQueryFactory queryFactory;

    @Override
    public List<UUID> findLikeReviewIds(UUID userId, List<UUID> reviewIds) {
        if (userId == null || reviewIds == null || reviewIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
            .select(reviewLike.review.id)
            .from(reviewLike)
            .where(
                reviewLike.user.id.eq(userId),
                reviewLike.review.id.in(reviewIds)
            )
            .fetch();
    }
}
