package com.sprint.deokhugam.review.application.port.out;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import jakarta.persistence.EntityManager;

import com.sprint.deokhugam.book.application.port.out.BookRepositoryPort;
import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.common.config.JpaConfig;
import com.sprint.deokhugam.common.config.QuerydslConfig;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.review.domain.ReviewLike;
import com.sprint.deokhugam.user.application.port.out.UserRepositoryPort;
import com.sprint.deokhugam.user.domain.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, QuerydslConfig.class})
class ReviewLikeRepositoryCustomImplTest {

    @Autowired
    private ReviewLikeRepositoryCustomImpl reviewLikeRepositoryCustom;

    @Autowired
    private UserRepositoryPort userRepository;

    @Autowired
    private BookRepositoryPort bookRepository;

    @Autowired
    private ReviewRepositoryPort reviewRepository;

    @Autowired
    private ReviewLikeRepositoryPort reviewLikeRepository;

    @Autowired
    private EntityManager entityManager;

    private User createMockUser(String email, String nickname) {
        User user = User.create(email, nickname, "password1234!");
        return userRepository.saveAndFlush(user);
    }

    private Book createMockBook(User user, String isbn) {
        Book book = Book.create(
            "스프링 부트 정복기",
            user,
            "토비",
            "자바 스프링 백엔드 개발을 위한 필독서",
            "수달 출판사",
            LocalDate.of(2026, 11, 1),
            isbn,
            "https://example.com"
        );
        return bookRepository.saveAndFlush(book);
    }

    @Test
    @DisplayName("유저 ID와 리뷰 ID 리스트를 넘기면 해당 유저가 좋아요를 누른 리뷰 ID 목록을 반환한다")
    void findLikeReviewIds_Success() {
        // given
        User author1 = createMockUser("author1@naver.com", "리뷰작성자1");
        User author2 = createMockUser("author2@naver.com", "리뷰작성자2");

        Book book1 = createMockBook(author1, "9788960773417");
        Book book2 = createMockBook(author2, "9788960773424"); // ISBN 중복 방지

        Review review1 = Review.create(author1, book1, "최고의 책 1", 5);
        Review review2 = Review.create(author2, book2, "최고의 책 2", 4);
        reviewRepository.saveAndFlush(review1);
        reviewRepository.saveAndFlush(review2);

        User loginUser = createMockUser("loginUser@naver.com", "좋아요누르는수달");

        ReviewLike reviewLike = ReviewLike.create(review1, loginUser);
        reviewLikeRepository.saveAndFlush(reviewLike);

        entityManager.clear();

        List<UUID> reviewIds = List.of(review1.getId(), review2.getId());

        // when
        List<UUID> likedReviewIds = reviewLikeRepositoryCustom.findLikeReviewIds(loginUser.getId(), reviewIds);

        // then
        assertThat(likedReviewIds).hasSize(1);
        assertThat(likedReviewIds).containsExactly(review1.getId());
    }

    @Test
    @DisplayName("파라미터가 유효하지 않으면 빈 리스트를 반환한다")
    void findLikeReviewIds_ReturnEmptyList_WhenParamIsInvalid() {
        // given
        UUID validUserId = UUID.randomUUID();
        List<UUID> validReviewIds = List.of(UUID.randomUUID());

        // when & then
        assertThat(reviewLikeRepositoryCustom.findLikeReviewIds(null, validReviewIds)).isEmpty();
        assertThat(reviewLikeRepositoryCustom.findLikeReviewIds(validUserId, null)).isEmpty();
        assertThat(reviewLikeRepositoryCustom.findLikeReviewIds(validUserId, List.of())).isEmpty();
    }

    @Test
    @DisplayName("리뷰 ID 목록은 존재하지만 해당 유저가 좋아요를 누른 리뷰가 없다면 빈 리스트를 반환한다")
    void findLikeReviewIds_ReturnEmptyList_WhenNoLikedReview() {
        // given
        User author = createMockUser("author3@naver.com", "리뷰작성자3");
        Book book = createMockBook(author, "9788960773431");
        Review review = Review.create(author, book, "평범한 책", 3);
        reviewRepository.saveAndFlush(review);

        User loginUser = createMockUser("anotherUser@naver.com", "좋아요안누른수달");

        entityManager.clear();

        List<UUID> reviewIds = List.of(review.getId());

        // when
        List<UUID> likedReviewIds = reviewLikeRepositoryCustom.findLikeReviewIds(loginUser.getId(), reviewIds);

        // then
        assertThat(likedReviewIds).isEmpty();
    }

    @Test
    @DisplayName("유저가 좋아요를 눌렀더라도 조회 대상 리뷰 ID 목록에 포함되지 않았다면 빈 리스트를 반환한다")
    void findLikeReviewIds_ReturnEmptyList_WhenLikedReviewNotRequested() {
        // given
        User author = createMockUser("author4@naver.com", "리뷰작성자4");
        Book book = createMockBook(author, "9788960773448");
        Review review = Review.create(author, book, "숨겨진 명작", 5);
        reviewRepository.saveAndFlush(review);

        User loginUser = createMockUser("loginUser2@naver.com", "바쁜수달");
        ReviewLike reviewLike = ReviewLike.create(review, loginUser);
        reviewLikeRepository.saveAndFlush(reviewLike);

        entityManager.clear();

        List<UUID> unrequestedReviewIds = List.of(UUID.randomUUID());

        // when
        List<UUID> likedReviewIds = reviewLikeRepositoryCustom.findLikeReviewIds(loginUser.getId(),
            unrequestedReviewIds);

        // then
        assertThat(likedReviewIds).isEmpty();
    }

    @Test
    @DisplayName("다른 유저가 좋아요를 누른 리뷰 ID를 요청하더라도 내 ID가 아니라면 빈 리스트를 반환한다")
    void findLikeReviewIds_ReturnEmptyList_WhenReviewLikedByOtherUser() {
        // given
        User author = createMockUser("author5@naver.com", "리뷰작성자5");
        Book book = createMockBook(author, "9788960773455");
        Review review = Review.create(author, book, "인기 많은 책", 5);
        reviewRepository.saveAndFlush(review);

        User activeUser = createMockUser("activeUser@naver.com", "행동하는수달");
        User passiveUser = createMockUser("passiveUser@naver.com", "가만히있는수달");

        ReviewLike reviewLike = ReviewLike.create(review, activeUser);
        reviewLikeRepository.saveAndFlush(reviewLike);

        entityManager.clear();

        List<UUID> reviewIds = List.of(review.getId());

        // when
        List<UUID> likedReviewIds = reviewLikeRepositoryCustom.findLikeReviewIds(passiveUser.getId(), reviewIds);

        // then
        assertThat(likedReviewIds).isEmpty();
    }
}
