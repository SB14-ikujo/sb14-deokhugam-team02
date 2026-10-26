package com.sprint.deokhugam.notification.application.port.out;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.notification.domain.Notification;
import com.sprint.deokhugam.notification.domain.QNotification;

@RequiredArgsConstructor
public class NotificationRepositoryCustomImpl implements NotificationRepositoryCustom {

    private static final int READ_RETENTION_DAYS = 7;
    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;
    private final QNotification notification = QNotification.notification;

    /**
     * flush() : 벌크 업데이트는 영속성 컨텍스트를 거치지 않으므로, 실행 전에 남은 변경사항을 DB에 먼저 반영
     * updatedAt : 마찬가지로 영속성 컨텍스트를 거치지 않아서 JPA Auditing가 적용되지 않기 때문에 직접 적용
     * clear() : DB와 영속성 컨텍스트의 값이 달라지지 않도록 비워서, 이후 조회 시 DB에서 새로 가져오게 함
     * 주의 : clear()로 영속성 컨텍스트 전체가 비워지므로, 같은 트랜잭션에서 이 메서드 호출 전에 조회한 엔티티는
     *       더 이상 관리되지 않음. 호출 후 엔티티가 필요하면 다시 조회해서 사용할 것
     */
    @Override
    public long markAllAsRead(UUID userId) {
        entityManager.flush();

        long updatedCount = queryFactory
            .update(notification)
            .set(notification.confirmed, true)
            .set(notification.updatedAt, Instant.now())
            .where(
                notification.user.id.eq(userId),
                notification.confirmed.isFalse()
            )
            .execute();

        entityManager.clear();

        return updatedCount;
    }

    @Override
    public Long countByUserId(UUID userId) {
        return queryFactory
            .select(notification.count())
            .from(notification)
            .where(
                notification.user.id.eq(userId)
            )
            .fetchOne();
    }

    @Override
    public List<Notification> findAllByUserIdWithCursor(
        UUID userId, Sort.Direction direction, Instant cursor,
        Instant after, Integer limit
    ) {
        return queryFactory
            .selectFrom(notification)
            .join(notification.review).fetchJoin()
            .where(
                notification.user.id.eq(userId),
                this.cursorCondition(direction, cursor, after)

            )
            .orderBy(this.orderSpecifiers(direction))
            .limit(limit + 1)
            .fetch();
    }

    @Override
    public long deleteReadNotificationsBefore(LocalDate baseDate) {
        Instant cutoff = baseDate.minusDays(READ_RETENTION_DAYS)
            .atStartOfDay(ZoneId.of("Asia/Seoul"))
            .toInstant();

        return queryFactory
            .delete(notification)
            .where(
                notification.confirmed.isTrue(),
                notification.updatedAt.lt(cutoff)
            )
            .execute();
    }

    // Where (Notification은 cursor와 after가 둘 다 createdAt이라 보조 조건 없이 createdAt만 비교)
    private BooleanExpression cursorCondition(Sort.Direction direction, Instant cursor, Instant after) {
        if (cursor == null) {
            return null;
        }
        QNotification notification = QNotification.notification;
        return direction.isDescending()
            ? notification.createdAt.lt(cursor)
            : notification.createdAt.gt(cursor);
    }

    // OrderBy
    private OrderSpecifier<?>[] orderSpecifiers(Sort.Direction direction) {
        QNotification notification = QNotification.notification;
        ComparableExpressionBase<?> target = notification.createdAt; // 필요 시 하단 동적 가져오기 사용
        Order currentDirection = "DESC".equalsIgnoreCase(direction.toString()) ? Order.DESC : Order.ASC;

        return new OrderSpecifier<?>[] {
            new OrderSpecifier<>(currentDirection, target),
            new OrderSpecifier<>(currentDirection, notification.id)};
    }
}
