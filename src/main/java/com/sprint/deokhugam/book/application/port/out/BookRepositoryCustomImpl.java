package com.sprint.deokhugam.book.application.port.out;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.book.domain.QBook;

@Slf4j
@RequiredArgsConstructor
public class BookRepositoryCustomImpl implements BookRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QBook book = QBook.book;

    @Override
    public List<Book> findAllByCursor(String keyword, String orderBy, Sort.Direction direction, String cursor,
        Instant after, Integer limit) {

        return queryFactory
            .selectFrom(book)
            .where(
                book.isDeleted.isFalse(),
                keywordCondition(keyword),
                cursorCondition(orderBy, direction, cursor, after)
            )
            .orderBy(this.orderSpecifiers(orderBy, direction))
            .limit(limit + 1)
            .fetch();
    }

    @Override
    public Long countByKeyword(String keyword) {
        return queryFactory
            .select(book.count())
            .from(book)
            .where(book.isDeleted.isFalse(), keywordCondition(keyword))
            .fetchOne();
    }

    // 조건절1. (WHERE)
    private BooleanExpression keywordCondition(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String pattern = "%" + value + "%";

        return book.title.like(pattern)
            .or(book.author.like(pattern))
            .or(book.isbn.like(pattern));
    }

    // 조건절2.
    private BooleanExpression cursorCondition(
        String orderBy, Sort.Direction direction,
        String cursor, Instant after
    ) {
        if (cursor == null || after == null) {
            return null;
        }

        return switch (orderBy) {
            case "publishedDate" -> {
                LocalDate targetDate = LocalDate.parse(cursor);
                yield direction.isDescending()
                    ? book.publishedDate.lt(targetDate)
                    .or(book.publishedDate.eq(targetDate).and(book.createdAt.lt(after)))
                    : book.publishedDate.gt(targetDate)
                    .or(book.publishedDate.eq(targetDate).and(book.createdAt.gt(after)));
            }
            case "rating" -> {
                Double value = Double.parseDouble(cursor);
                yield direction.isDescending()
                    ? book.rating.lt(value).or(book.rating.eq(value).and(book.createdAt.lt(after)))
                    : book.rating.gt(value).or(book.rating.eq(value).and(book.createdAt.gt(after)));
            }
            case "reviewCount" -> {
                Integer value = Integer.parseInt(cursor);
                yield direction.isDescending()
                    ? book.reviewCount.lt(value).or(book.reviewCount.eq(value).and(book.createdAt.lt(after)))
                    : book.reviewCount.gt(value).or(book.reviewCount.eq(value).and(book.createdAt.gt(after)));
            }
            default -> direction.isDescending()
                ? book.title.lt(cursor).or(book.title.eq(cursor).and(book.createdAt.lt(after)))
                : book.title.gt(cursor).or(book.title.eq(cursor).and(book.createdAt.gt(after)));
        };
    }

    // 정렬
    private OrderSpecifier<?>[] orderSpecifiers(String orderBy, Sort.Direction direction) {
        ComparableExpressionBase<?> target = getSortExpression(orderBy);
        Order order = direction.isDescending() ? Order.DESC : Order.ASC;

        return new OrderSpecifier<?>[] {
            new OrderSpecifier<>(order, target),
            new OrderSpecifier<>(order, book.createdAt)
        };
    }

    private ComparableExpressionBase<?> getSortExpression(String orderByField) {
        return switch (orderByField) {
            case "publishedDate" -> book.publishedDate;
            case "rating" -> book.rating;
            case "reviewCount" -> book.reviewCount;
            default -> book.title;
        };
    }
}
