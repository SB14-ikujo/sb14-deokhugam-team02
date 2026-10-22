package com.sprint.deokhugam.notification.application.port.out;

import java.time.Instant;
import java.util.UUID;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.notification.domain.QNotification;

@RequiredArgsConstructor
public class NotificationRepositoryCustomImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    /**
     * flush() : 벌크 업데이트는 영속성 컨텍스트를 거치지 않으므로, 실행 전에 남은 변경사항을 DB에 먼저 반영
     * updatedAt : 마찬가지로 영속성 컨텍스트를 거치지 않아서 JPA Auditing가 적용되지 않기 때문에 직접 적용
     * clear() : DB와 영속성 컨텍스트의 값이 달라지지 않도록 비워서, 이후 조회 시 DB에서 새로 가져오게 함
     *
     * 주의 : clear()로 영속성 컨텍스트 전체가 비워지므로, 같은 트랜잭션에서 이 메서드 호출 전에 조회한 엔티티는
     *       더 이상 관리되지 않음. 호출 후 엔티티가 필요하면 다시 조회해서 사용할 것
     */
    @Override
    public long markAllAsRead(UUID userId) {
        QNotification notification = QNotification.notification;
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
}
