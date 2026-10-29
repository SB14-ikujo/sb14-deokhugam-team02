package com.sprint.deokhugam.user.application.port.out;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import jakarta.persistence.EntityManager;

import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.comment.domain.Comment;
import com.sprint.deokhugam.common.config.JpaConfig;
import com.sprint.deokhugam.common.config.QuerydslConfig;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.review.domain.ReviewLike;
import com.sprint.deokhugam.user.domain.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, QuerydslConfig.class})
class UserRepositoryPortTest {
    @Autowired
    private UserRepositoryPort userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("이미 가입된 이메일이면 true를 반환한다.")
    void existsByEmail() {
        // given
        userRepository.saveAndFlush(User.create("user@naver.com", "닉네임", "encoded"));

        // when & then
        assertThat(userRepository.existsByEmail("user@naver.com")).isTrue();
        assertThat(userRepository.existsByEmail("other@naver.com")).isFalse();
    }

    @Test
    @DisplayName("논리 삭제된 사용자는 ID로 조회되지 않는다.")
    void findByIdAndIsDeletedFalse() {
        // given
        User activeUser = userRepository.saveAndFlush(User.create("a@naver.com", "활성", "encoded"));
        User deletedUser = User.create("d@naver.com", "삭제", "encoded");
        deletedUser.delete();
        userRepository.saveAndFlush(deletedUser);

        // when & then
        assertThat(userRepository.findByIdAndIsDeletedFalse(activeUser.getId())).isPresent();
        assertThat(userRepository.findByIdAndIsDeletedFalse(deletedUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("논리 삭제된 사용자는 이메일로 조회되지 않는다.")
    void findByEmailAndIsDeletedFalse() {
        // given
        User deletedUser = User.create("d@naver.com", "삭제", "encoded");
        deletedUser.delete();
        userRepository.saveAndFlush(deletedUser);

        // when & then
        assertThat(userRepository.findByEmailAndIsDeletedFalse("d@naver.com")).isEmpty();
    }

    @Test
    @DisplayName("사용자를 논리 삭제하면 update_at이 갱신된다.")
    void delete_updatesUpdatedAt() {
        // given
        User user = userRepository.saveAndFlush(User.create("delete@naver.com", "탈퇴합니다", "encoded"));
        Instant beforeDelete = user.getUpdatedAt();

        // when
        user.delete();
        userRepository.saveAndFlush(user);

        // then
        assertThat(user.getUpdatedAt()).isAfter(beforeDelete);
    }

    @Test
    @DisplayName("사용자를 물리 삭제하면 그 사용자의 리뷰는 함께 삭제되고, 등록한 도서는 등록자만 비워진 채 남는다.")
    void delete_cascadesReviewAndKeepsBook() {
        // given
        User user = userRepository.saveAndFlush(User.create("hard@naver.com", "물리삭제", "encoded"));
        Book book = persistBook(user, "user-hard-delete-isbn");
        Review review = Review.create(user, book, "리뷰 내용", 5);
        entityManager.persist(review);
        entityManager.flush();
        entityManager.clear();

        // when
        userRepository.deleteById(user.getId());
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(entityManager.find(Review.class, review.getId())).isNull();
        Book foundBook = entityManager.find(Book.class, book.getId());
        assertThat(foundBook).isNotNull();
        assertThat(foundBook.getUser()).isNull();
    }

    @Test
    @DisplayName("사용자를 물리 삭제하면 다른 사람 리뷰에 남긴 댓글과 좋아요는 삭제되고, 그 리뷰는 남는다.")
    void delete_cascadesCommentAndLikeOnOthersReview() {
        // given
        User writer = userRepository.saveAndFlush(User.create("writer@naver.com", "작성자", "encoded"));
        User leaver = userRepository.saveAndFlush(User.create("leaver@naver.com", "탈퇴자", "encoded"));
        Book book = persistBook(writer, "user-hard-delete-isbn");
        Review review = Review.create(writer, book, "리뷰 내용", 5);
        entityManager.persist(review);
        Comment comment = Comment.create(review, leaver, "댓글 내용");
        entityManager.persist(comment);
        ReviewLike like = ReviewLike.create(review, leaver);
        entityManager.persist(like);
        entityManager.flush();
        entityManager.clear();

        // when
        userRepository.deleteById(leaver.getId());
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(entityManager.find(Comment.class, comment.getId())).isNull();
        assertThat(entityManager.find(ReviewLike.class, like.getId())).isNull();
        assertThat(entityManager.find(Review.class, review.getId())).isNotNull();
    }

    private Book persistBook(User registrant, String isbn) {
        Book book = Book.create("도서", registrant, "저자", "설명", "출판사",
            LocalDate.of(2026, 1, 1), isbn, null);
        entityManager.persist(book);
        return book;
    }

}
