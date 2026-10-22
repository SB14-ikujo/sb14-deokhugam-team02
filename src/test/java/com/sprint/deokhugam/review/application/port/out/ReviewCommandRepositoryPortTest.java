package com.sprint.deokhugam.review.application.port.out;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.user.domain.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({com.sprint.deokhugam.common.config.JpaConfig.class, com.sprint.deokhugam.common.config.QuerydslConfig.class})
public class ReviewCommandRepositoryPortTest { // 💡 중복 선언을 없애고 깔끔하게 하나로 통일!

    @Autowired
    private ReviewCommandRepositoryPort reviewCommandRepositoryPort;

    @Autowired
    private TestEntityManager entityManager; // 💡 임포트를 깔끔하게 정리하여 주입 완료!

    @Test
    @DisplayName("increaseLikeCount를 호출하면 해당 리뷰의 좋아요 수가 실제로 1 증가한다.")
    void increaseLikeCount_success() {
        // Given - 가짜 객체들 준비
        User user = User.create("test@naver.com", "강아지", "1234");
        Book book = Book.create(
            "도서제목1",
            "작가1",
            "설명1",
            "퍼블리셔1",
            LocalDate.now(),
            "9788960773431",
            "https://example.com"
        );

        // 부모 객체인 유저와 도서를 DB에 먼저 영속화
        entityManager.persist(user);
        entityManager.persist(book);
        entityManager.flush();

        Review review = Review.create(user, book, "리뷰 내용 테스트1", 5);
        reviewCommandRepositoryPort.save(review);
        UUID reviewId = review.getId();

        // When - 벌크 수정 (+1) 쿼리 실행
        reviewCommandRepositoryPort.increaseLikeCount(reviewId);

        // Then - 영속성 컨텍스트가 초기화되었으므로 새로 조회해서 값 검증
        Review updatedReview = reviewCommandRepositoryPort.findById(reviewId)
            .orElseThrow(() -> new AssertionError("리뷰를 찾을 수 없습니다."));

        assertThat(updatedReview.getLikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("decreaseLikeCount를 호출하면 해당 리뷰의 좋아요 수가 실제로 1 감소한다.")
    void decreaseLikeCount_success() {
        // [Given] 가짜 객체들 준비
        User user = User.create("test@naver.com", "강아지", "1234");
        Book book = Book.create("도서제목1", "작가1", "설명1", "퍼블리셔1", LocalDate.now(), "9788960773431",
            "https://example.com");

        entityManager.persist(user);
        entityManager.persist(book);
        entityManager.flush();

        Review review = Review.create(user, book, "리뷰 내용 테스트1", 5);
        review.upLikeCount(); // 기본 좋아요 1개 세팅

        reviewCommandRepositoryPort.save(review);
        UUID reviewId = review.getId();

        // [When] 벌크 수정 (-1) 쿼리 실행
        reviewCommandRepositoryPort.decreaseLikeCount(reviewId);

        // [Then] 새로 조회해서 검증
        Review updatedReview = reviewCommandRepositoryPort.findById(reviewId)
            .orElseThrow(() -> new AssertionError("리뷰를 찾을 수 없습니다."));

        assertThat(updatedReview.getLikeCount()).isEqualTo(0);
    }
}
