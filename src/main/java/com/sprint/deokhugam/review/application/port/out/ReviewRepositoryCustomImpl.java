package com.sprint.deokhugam.review.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.review.domain.QReview;
import com.sprint.deokhugam.review.domain.Review;

@RequiredArgsConstructor
public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {

    private static final QReview review = QReview.review;
    private final JPAQueryFactory queryFactory;

    @Override
    public long countByConditions(UUID userId, UUID bookId, String keyword) {
        Long totalCount = queryFactory
            .select(review.count())
            .from(review)
            .join(review.user)
            .join(review.book)
            .where(
                userIdEq(userId),
                bookIdEq(bookId),
                keywordContains(keyword)
            )
            .fetchOne();

        return (totalCount != null) ? totalCount : 0L;
    }

    @Override
    public List<Review> getReviewList(
        UUID userId,
        UUID bookId,
        String keyword,
        String orderBy,
        Instant after,
        String cursor,
        Sort.Direction direction,
        int size
    ) {

        boolean asc = (direction == Sort.Direction.ASC);

        // orderBy = "rating"(평점), "createdAt"(생성시간)
        boolean byRating = "rating".equals(orderBy);

        return queryFactory
            .selectFrom(review)
            .join(review.user).fetchJoin()
            .join(review.book).fetchJoin()
            .where(
                userIdEq(userId),
                bookIdEq(bookId),
                keywordContains(keyword),
                byRating ? ratingCursor(after, cursor, asc) : createdAtCursor(after, asc)
            )
            .orderBy(getSortOrder(orderBy, asc))
            .limit(size + 1)
            .fetch();
    }

    // < WHERE 절 >
    // 특정 유저 필터
    private BooleanExpression userIdEq(UUID userId) {
        return (userId != null) ? review.user.id.eq(userId) : null;
    }

    // 특정 도서 필터
    private BooleanExpression bookIdEq(UUID bookId) {
        return (bookId != null) ? review.book.id.eq(bookId) : null;
    }

    // 검색 조건 (리뷰 작성자 닉네임, 리뷰 내용, 리뷰한 도서명)
    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return review.user.nickname.contains(keyword)
            .or(review.content.contains(keyword))
            .or(review.book.title.contains(keyword));
    }

    // orderBy = ASC, DESC
    // cursor - 생성시간(createdAt)
    // after - 생성시간(createdAt)
    private BooleanExpression createdAtCursor(Instant after, boolean asc) {
        if (after == null) {
            return null;
        }
        return asc ? review.createdAt.gt(after) : review.createdAt.lt(after);
    }

    // orderBy = ASC, DESC
    // cursor = 리뷰 평점
    // after = 생성시간
    private BooleanExpression ratingCursor(Instant after, String cursor, boolean asc) {
        //  첫 페이지 요청이거나 값이 없는 경우
        if (after == null || cursor == null || cursor.isBlank()) {
            return null;
        }

        // 평점을 정수로 변환
        int rating = (int)Double.parseDouble(cursor);

        // 평점순 - 오름차순
        if (asc) {
            return review.rating.gt(rating)
                .or(review.rating.eq(rating).and(review.createdAt.gt(after)));
        }

        // 평점순 - 내림차순
        return review.rating.lt(rating)
            .or(review.rating.eq(rating).and(review.createdAt.lt(after)));
    }

    // < ORDER BY 절 >
    private OrderSpecifier<?>[] getSortOrder(String orderBy, boolean asc) {

        // 평점순 - 정렬일 때
        if ("rating".equals(orderBy)) {
            return asc ? new OrderSpecifier<?>[] {review.rating.asc(), review.createdAt.asc()} :
                new OrderSpecifier<?>[] {review.rating.desc(), review.createdAt.desc()}; // 높은 평점순, 최신 순
        }

        // 시간순 - 정렬일 때
        return asc ? new OrderSpecifier<?>[] {review.createdAt.asc()} : // 오래된 순
            new OrderSpecifier<?>[] {review.createdAt.desc()}; // 최신 순
    }
}

