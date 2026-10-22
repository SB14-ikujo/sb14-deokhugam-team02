package com.sprint.deokhugam.notification.application.port.out;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

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

    private User user;

    @BeforeEach
    void setUp() {
        user = entityManager.persist(User.create("repo-test1@test.com", "수신자", "password1!"));

        Book book = Book.create("테스트 도서", "저자", "설명", "출판사",
            LocalDate.of(2020, 1, 1), "9780000000001", null);
        ReflectionTestUtils.setField(book, "user", user); // Book.create()는 user를 받지 않음
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
}
