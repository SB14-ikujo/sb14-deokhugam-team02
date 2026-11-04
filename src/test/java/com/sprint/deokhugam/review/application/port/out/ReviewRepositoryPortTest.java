package com.sprint.deokhugam.review.application.port.out;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import jakarta.persistence.EntityManager;

import com.sprint.deokhugam.book.application.port.out.BookRepositoryPort;
import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.common.config.JpaConfig;
import com.sprint.deokhugam.common.config.QuerydslConfig;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.user.application.port.out.UserRepositoryPort;
import com.sprint.deokhugam.user.domain.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, QuerydslConfig.class})
class ReviewRepositoryPortTest {
    @Autowired
    private ReviewRepositoryPort reviewRepository;

    @Autowired
    private UserRepositoryPort userRepository;

    @Autowired
    private BookRepositoryPort bookRepository;

    @Autowired
    private EntityManager entityManager;

    private User createMockUser() {
        User user = User.create("abcd@naver.com", "수달짱", "password1234!");
        return userRepository.saveAndFlush(user);
    }

    private Book createMockBook(User user) {
        Book book = Book.create(
            "스프링 부트 정복기",
            user,
            "토비",
            "자바 스프링 백엔드 개발을 위한 필독서",
            "수달 출판사",
            LocalDate.of(2026, 11, 1),
            "9788960773417",
            "https://example.com"
        );
        return bookRepository.saveAndFlush(book);
    }

    // ReviewRepositoryPort
    @Test
    @DisplayName("리뷰 id를 넣으면 논리 삭제된 리뷰들은 제외한 리뷰데이터를 옵셔널에 담아서 가지고 온다")
    void findByIdAndDeletedFalse() {
        // given
        User user = createMockUser();
        Book book1 = createMockBook(user);

        Book book2 = Book.create(
            "스프링 부트 정복기1234",
            user,
            "토비",
            "자바 스프링 백엔드 개발을 위한 필독서",
            "수달 출판사",
            LocalDate.of(2026, 11, 1),
            "9788960773423",
            "https://example2.com"
        );
        ;
        bookRepository.saveAndFlush(book2);
        entityManager.clear();

        Review activeReview = Review.create(user, book1, "재밌네요.", 5);
        reviewRepository.saveAndFlush(activeReview);

        Review deletedReview = Review.create(user, book2, "재미없네요.", 1);
        deletedReview.softDelete(user.getId());
        reviewRepository.saveAndFlush(deletedReview); //
        entityManager.clear();

        // when & then
        assertThat(reviewRepository.findByIdAndIsDeletedFalse(activeReview.getId())).isPresent();
        assertThat(reviewRepository.findByIdAndIsDeletedFalse(deletedReview.getId())).isEmpty();
    }

    // ReviewCommandRepositoryPort
    @Test
    @DisplayName("유저 ID와 도서 ID로 조회 시 삭제되지 않은 리뷰만 정상적으로 반환된다")
    void findValidReview() {
        // given
        User user = createMockUser();
        Book book1 = createMockBook(user);

        Book book2 = Book.create(
            "스프링 부트 정복기1234",
            user,
            "토비",
            "자바 스프링 백엔드 개발을 위한 필독서",
            "수달 출판사",
            LocalDate.of(2026, 11, 1),
            "9788960773423",
            "https://example2.com"
        );

        bookRepository.saveAndFlush(book2);
        entityManager.clear();

        Review activeReview = Review.create(user, book1, "재밌네요.", 5);
        reviewRepository.saveAndFlush(activeReview);

        Review deletedReview = Review.create(user, book2, "재미없네요.", 1);
        deletedReview.softDelete(user.getId());
        reviewRepository.saveAndFlush(deletedReview);
        entityManager.clear();

        // when
        Optional<Review> realReview = reviewRepository.findValidReview(user.getId(), book1.getId());
        Optional<Review> softDeletedReview = reviewRepository.findValidReview(user.getId(), book2.getId());

        // then
        assertThat(realReview).isPresent();
        assertThat(softDeletedReview).isEmpty();
    }

    @Test
    @DisplayName("유저와 도서를 패치조인해서 한번에 가져온 리뷰")
    void findByFetchJoin() {
        // given
        User user = createMockUser();
        Book book1 = createMockBook(user);

        Book book2 = Book.create(
            "스프링 부트 정복기1234",
            user,
            "토비",
            "자바 스프링 백엔드 개발을 위한 필독서",
            "수달 출판사",
            LocalDate.of(2026, 11, 1),
            "9788960773423",
            "https://example2.com"
        );
        ;
        bookRepository.saveAndFlush(book2);
        entityManager.clear();

        Review activeReview = Review.create(user, book1, "재밌네요.", 5);
        reviewRepository.saveAndFlush(activeReview);

        Review deletedReview = Review.create(user, book2, "재미없네요.", 1);
        deletedReview.softDelete(user.getId());
        reviewRepository.saveAndFlush(deletedReview); //
        entityManager.clear();

        // when & then
        Optional<Review> activeResult = reviewRepository.findByFetchJoin(activeReview.getId());
        assertThat(activeResult).isPresent();
        assertThat(activeResult.get().getUser().getNickname()).isEqualTo("수달짱");
        assertThat(activeResult.get().getBook().getTitle()).isEqualTo("스프링 부트 정복기");

        Optional<Review> deletedResult = reviewRepository.findByFetchJoin(deletedReview.getId());
        assertThat(deletedResult).isEmpty();
    }

    @Test
    @DisplayName("리뷰의 '좋아요 수' 증가")
    void increaseLikeCount() {
        // given
        User user = createMockUser();
        Book book = createMockBook(user);

        Review activeReview = Review.create(user, book, "재밌네요", 5);
        reviewRepository.saveAndFlush(activeReview);

        // when
        reviewRepository.increaseLikeCount(activeReview.getId());

        // then
        Review updatedActiveReview = reviewRepository.findById(activeReview.getId()).get();
        assertThat(updatedActiveReview.getLikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("리뷰의 '좋아요 수' 감소 (0 이하로 안 내려가는 예외 조건 포함)")
    void decreaseLikeCount() {
        // given
        User user = createMockUser();
        Book book = createMockBook(user);

        Review activeReview = Review.create(user, book, "재밌네요", 5);
        reviewRepository.saveAndFlush(activeReview);

        // when & then
        reviewRepository.increaseLikeCount(activeReview.getId());
        reviewRepository.decreaseLikeCount(activeReview.getId());

        Review review1 = reviewRepository.findById(activeReview.getId()).get();
        assertThat(review1.getLikeCount()).isEqualTo(0);

        reviewRepository.decreaseLikeCount(activeReview.getId());
        Review review2 = reviewRepository.findById(activeReview.getId()).get();
        assertThat(review2.getLikeCount()).isEqualTo(0);
    }

    // ReviewRepositoryCustom
    @Test
    @DisplayName("리뷰 목록 최신순 조회: 가장 최근에 작성된 리뷰부터 정렬되어 반환된다")
    void getReviewList_orderByCreatedAt() {
        // given
        User user1 = createMockUser();
        Book book = createMockBook(user1);

        User user2 = User.create("other_user@naver.com", "수달동생", "password1234!");
        userRepository.saveAndFlush(user2);

        Review activeReview1 = Review.create(user1, book, "옛날 리뷰", 5);
        reviewRepository.saveAndFlush(activeReview1);

        Review activeReview2 = Review.create(user2, book, "최신 리뷰", 1);
        reviewRepository.saveAndFlush(activeReview2);

        entityManager.clear();

        // When
        List<Review> result = reviewRepository.getReviewList(
            null,
            null,
            null,
            "createdAt",
            null,
            null,
            Sort.Direction.DESC,
            2
        );

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("최신 리뷰");
        assertThat(result.get(1).getContent()).isEqualTo("옛날 리뷰");
    }

    @Test
    @DisplayName("리뷰 목록 평점순 조회: 평점이 높은 순서대로 정렬되어 반환된다")
    void getReviewList_orderByRating() {
        // given
        User user = createMockUser();
        Book book1 = createMockBook(user);

        Book book2 = Book.create(
            "스프링 부트 정복기1234", user, "토비", "상세설명", "수달 출판사",
            LocalDate.of(2026, 11, 1), "9788960773423", "https://example2.com"
        );
        bookRepository.saveAndFlush(book2);

        Review activeReview1 = Review.create(user, book1, "옛날 리뷰", 5);
        reviewRepository.saveAndFlush(activeReview1);

        Review activeReview2 = Review.create(user, book2, "최신 리뷰", 1);
        reviewRepository.saveAndFlush(activeReview2);

        entityManager.clear();

        PageRequest pageRequest = PageRequest.of(0, 2);

        // When
        List<Review> result = reviewRepository.getReviewList(
            user.getId(),
            null,
            null,
            "rating",
            null,
            null,
            Sort.Direction.DESC,
            2
        );

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRating()).isEqualTo(5);
        assertThat(result.get(1).getRating()).isEqualTo(1);
    }

    @Test
    @DisplayName("조건(유저, 도서, 키워드)에 맞는 전체 리뷰의 총 개수를 셈")
    void countByConditions() {
        // given
        User user1 = createMockUser();
        Book book = createMockBook(user1);

        User user2 = User.create("user2@naver.com", "수달친구", "password1234!");
        userRepository.saveAndFlush(user2);

        User user3 = User.create("user3@naver.com", "수달동생", "password1234!");
        userRepository.saveAndFlush(user3);

        Review review1 = Review.create(user1, book, "재밌네요.", 5);
        Review review2 = Review.create(user2, book, "공부하기 좋아요.", 4);
        Review review3 = Review.create(user3, book, "지루해요.", 2);

        reviewRepository.saveAndFlush(review1);
        reviewRepository.saveAndFlush(review2);
        reviewRepository.saveAndFlush(review3);

        entityManager.clear();

        // When & Then
        long countStrict = reviewRepository.countByConditions(null, book.getId(), "재밌네요");
        org.assertj.core.api.Assertions.assertThat(countStrict).isEqualTo(1L);

        long countWithBookTitle = reviewRepository.countByConditions(null, book.getId(), "스프링");
        org.assertj.core.api.Assertions.assertThat(countWithBookTitle).isEqualTo(3L);

        long countZero = reviewRepository.countByConditions(null, book.getId(), "치킨");
        org.assertj.core.api.Assertions.assertThat(countZero).isEqualTo(0L);
    }

    @Test
    @DisplayName("모든 필터(유저, 도서, 키워드)를 동시에 걸었을 때 정확히 매칭되는 개수를 센다")
    void countByConditions_AllFilters() {
        // given
        User user = createMockUser();
        Book book = createMockBook(user);
        Review review = Review.create(user, book, "완벽한 매칭 리뷰", 5);
        reviewRepository.saveAndFlush(review);
        entityManager.clear();

        // when
        long count = reviewRepository.countByConditions(user.getId(), book.getId(), "완벽한");

        // then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("유저 닉네임으로 키워드 검색 시 정확하게 카운트된다")
    void countByConditions_KeywordNickname() {
        // given
        User user = User.create("nickname-test@naver.com", "특별한닉네임수달", "password1234!");
        userRepository.saveAndFlush(user);
        Book book = createMockBook(user);
        Review review = Review.create(user, book, "평범한 내용", 4);
        reviewRepository.saveAndFlush(review);
        entityManager.clear();

        // when
        long count = reviewRepository.countByConditions(null, null, "특별한");

        // then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("무의미한 빈 검색어가 들어왔을 때 동적 쿼리가 무시하고 전체 조회하는지 검증한다")
    void getReviewList_WithEmptyKeyword() {
        // given
        User user = createMockUser();
        Book book = createMockBook(user);
        Review review = Review.create(user, book, "테스트", 5);
        reviewRepository.saveAndFlush(review);
        entityManager.clear();

        // when
        List<Review> resultWithBlank = reviewRepository.getReviewList(
            null, null, "   ", "createdAt", null, null, org.springframework.data.domain.Sort.Direction.DESC, 10
        );

        // then
        assertThat(resultWithBlank).isNotEmpty();
    }

}
