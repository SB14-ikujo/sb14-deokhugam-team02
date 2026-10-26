package com.sprint.deokhugam.notification.application.port.out;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;

import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.common.config.JpaConfig;
import com.sprint.deokhugam.common.config.QuerydslConfig;
import com.sprint.deokhugam.notification.domain.Notification;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.user.domain.User;

@DataJpaTest
@Import({JpaConfig.class, QuerydslConfig.class}) // 우리가 만든 설정 파일 Spring 자동 주입설정
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NotificationRepositoryCustomImplTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepositoryCustomImpl notificationRepository;

    private static final Instant BASE = Instant.parse("2026-10-01T00:00:00Z");
    private static final LocalDate BASE_DATE = LocalDate.of(2026, 10, 26);
    // 기준일 - 7일 00:00 (Asia/Seoul)
    private static final Instant CUTOFF = LocalDate.of(2026, 10, 19).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant();

    private User user;

    @BeforeEach
    void setUp() {
        user = entityManager.persist(User.create("repo-test1@test.com", "수신자", "password1!"));

        Book book = Book.create("테스트 도서", user, "저자", "설명", "출판사",
            LocalDate.of(2020, 1, 1), "9780000000001", null);
        entityManager.persist(book);

        Review review = entityManager.persist(Review.create(user, book, "테스트 리뷰", 5));

        for (int i = 1; i <= 3; i++) {
            entityManager.persist(Notification.create(review, "[덕후" + i + "]님이 나의 리뷰를 좋아합니다."));
        }
        for (int i = 4; i <= 5; i++) {
            Notification read = Notification.create(review, "[덕후" + i + "]님이 나의 리뷰에 댓글을 남겼습니다.");
            read.markAsRead();
            entityManager.persist(read);
        }

        entityManager.flush(); // DB 반영 (테스트 종료 시 Transactional을 통해 롤백)
        entityManager.clear(); // 영속 컨텍스트 초기화
    }

    @Test
    @DisplayName("존재하지 않는 사용자면 0을 반환한다.")
    void markAllAsRead_returnsZeroWhenUserNotExists() {
        // when
        long updatedCount = notificationRepository.markAllAsRead(UUID.randomUUID());

        // then
        assertThat(updatedCount).isZero();
    }

    @Test
    @DisplayName("사용자의 알림 3개를 전체 읽음 처리하면 3을 반환한다.")
    void markAllAsRead_returnsThreeWhenNotificationsExist() {
        // when
        long updatedCount = notificationRepository.markAllAsRead(user.getId());

        // then
        assertThat(updatedCount).isEqualTo(3);
    }

    @Test
    @DisplayName("사용자의 알림 개수를 반환한다.")
    void countByUserId_returnsCount() {
        // when
        Long count = notificationRepository.countByUserId(user.getId());

        // then
        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("커서가 없으면 DESC 정렬로 limit + 1개를 반환한다.")
    void findAllByUserIdWithCursor_withoutCursorDesc() {
        // given
        fixCreatedAtByMessageOrder();

        // when
        List<Notification> result = notificationRepository
            .findAllByUserIdWithCursor(user.getId(), Sort.Direction.DESC, null, null, 2);

        // then
        assertThat(result).extracting(Notification::getCreatedAt)
            .containsExactly(time(5), time(4), time(3));
    }

    @Test
    @DisplayName("DESC 커서가 있으면 커서보다 이전 알림만 반환하고 커서 항목은 제외한다.")
    void findAllByUserIdWithCursor_withCursorDesc() {
        // given
        fixCreatedAtByMessageOrder();

        // when
        List<Notification> result = notificationRepository
            .findAllByUserIdWithCursor(user.getId(), Sort.Direction.DESC, time(4), null, 10);

        // then
        assertThat(result).extracting(Notification::getCreatedAt)
            .containsExactly(time(3), time(2), time(1));
    }

    @Test
    @DisplayName("ASC 커서가 있으면 커서보다 이후 알림만 오름차순으로 반환하고 커서 항목은 제외한다.")
    void findAllByUserIdWithCursor_withCursorAsc() {
        // given
        fixCreatedAtByMessageOrder();

        // when
        List<Notification> result = notificationRepository
            .findAllByUserIdWithCursor(user.getId(), Sort.Direction.ASC, time(2), null, 10);

        // then
        assertThat(result).extracting(Notification::getCreatedAt)
            .containsExactly(time(3), time(4), time(5));
    }

    @Test
    @DisplayName("기준일 7일 전 시각보다 오래된 읽은 알림만 삭제한다.")
    void deleteReadNotificationsBefore_deletesOnlyOldReadNotifications() {
        // given: 덕후4(읽음, 기준 시각 1초 전) → 삭제 대상, 덕후5(읽음, 방금 전) → 유지
        fixUpdatedAt(4, CUTOFF.minusSeconds(1));

        // when
        long deleted = notificationRepository.deleteReadNotificationsBefore(BASE_DATE);

        // then
        assertThat(deleted).isEqualTo(1);
        assertThat(remainingMessages()).hasSize(4)
            .noneMatch(message -> message.startsWith("[덕후4]"));
    }

    @Test
    @DisplayName("기준 시각과 정확히 같은 시각에 읽은 알림은 삭제하지 않는다.")
    void deleteReadNotificationsBefore_keepsNotificationAtCutoff() {
        // given
        fixUpdatedAt(4, CUTOFF);

        // when
        long deleted = notificationRepository.deleteReadNotificationsBefore(BASE_DATE);

        // then
        assertThat(deleted).isZero();
        assertThat(remainingMessages()).hasSize(5);
    }

    @Test
    @DisplayName("오래됐더라도 읽지 않은 알림은 삭제하지 않는다.")
    void deleteReadNotificationsBefore_keepsOldUnreadNotifications() {
        // given: 안 읽은 알림(덕후1~3)을 오래된 시각으로 변경
        for (int i = 1; i <= 3; i++) {
            fixUpdatedAt(i, CUTOFF.minus(30, ChronoUnit.DAYS));
        }

        // when
        long deleted = notificationRepository.deleteReadNotificationsBefore(BASE_DATE);

        // then
        assertThat(deleted).isZero();
        assertThat(remainingMessages()).hasSize(5);
    }

    @Test
    @DisplayName("삭제 대상이 없으면 0을 반환한다.")
    void deleteReadNotificationsBefore_returnsZeroWhenNothingToDelete() {
        // when: 읽은 알림(덕후4, 5)의 updatedAt이 방금 전이라 삭제 대상 아님
        long deleted = notificationRepository.deleteReadNotificationsBefore(BASE_DATE);

        // then
        assertThat(deleted).isZero();
        assertThat(remainingMessages()).hasSize(5);
    }

    private Instant time(int index) {
        return BASE.plusSeconds(index);
    }

    private List<String> remainingMessages() {
        entityManager.clear();
        return entityManager.getEntityManager()
            .createQuery("select n.message from Notification n", String.class)
            .getResultList();
    }

    /**
     * Auditing으로 createdAt이 거의 같은 시각에 찍히므로, 메시지 번호(덕후1~5) 순서대로 createdAt을 고정한다.
     * createdAt은 updatable = false라 네이티브 쿼리로 직접 변경한다.
     */
    private void fixCreatedAtByMessageOrder() {

        for (int i = 1; i <= 5; i++) {
            entityManager.getEntityManager()
                .createNativeQuery("update notifications set created_at = :createdAt where message like :message")
                .setParameter("createdAt", time(i))
                .setParameter("message", "[덕후" + i + "]%")
                .executeUpdate();
        }
        entityManager.clear();
    }

    /**
     * updatedAt은 Auditing이 관리하므로 네이티브 쿼리로 메시지 번호(덕후N)의 알림 updatedAt을 직접 변경한다.
     */
    private void fixUpdatedAt(int index, Instant updatedAt) {
        entityManager.getEntityManager()
            .createNativeQuery("update notifications set updated_at = :updatedAt where message like :message")
            .setParameter("updatedAt", updatedAt)
            .setParameter("message", "[덕후" + index + "]%")
            .executeUpdate();
        entityManager.clear();
    }
}
