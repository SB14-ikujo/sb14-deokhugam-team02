package com.sprint.deokhugam.comment;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
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
import org.springframework.test.util.ReflectionTestUtils;

import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.comment.application.port.out.CommentRepositoryPort;
import com.sprint.deokhugam.comment.domain.Comment;
import com.sprint.deokhugam.common.config.JpaConfig;
import com.sprint.deokhugam.common.config.QuerydslConfig;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.user.domain.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, QuerydslConfig.class})
class CommentRepositoryPortTest {

    @Autowired
    private CommentRepositoryPort commentRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private Review review;

    @BeforeEach
    void setUp() {
        user = em.persist(User.create("test@test.com", "덕후", "password1!"));

        Book book = Book.create(
            "제목",
            user,
            "저자",
            "설명",
            "출판사",
            LocalDate.of(2024, 1, 1),
            "9781234567890",
            null
        );

        ReflectionTestUtils.setField(book, "user", user);
        em.persist(book);

        review = em.persist(Review.create(user, book, "리뷰 내용", 5));
    }

    private Comment saveComment(String content) throws InterruptedException {
        Comment comment = em.persistAndFlush(
            Comment.create(review, user, content)
        );
        Thread.sleep(10);
        return comment;
    }

    @Test
    @DisplayName("논리 삭제되지 않은 댓글은 id로 조회된다")
    void findByIdAndIsDeletedFalse_found() throws InterruptedException {
        Comment comment = saveComment("댓글");

        assertThat(
            commentRepository.findByIdAndIsDeletedFalse(comment.getId())
        ).isPresent();
    }

    @Test
    @DisplayName("논리 삭제된 댓글은 id로 조회되지 않는다")
    void findByIdAndIsDeletedFalse_deleted() throws InterruptedException {
        Comment comment = saveComment("댓글");
        comment.softDelete();

        em.flush();
        em.clear();

        assertThat(
            commentRepository.findByIdAndIsDeletedFalse(comment.getId())
        ).isEmpty();
    }

    @Test
    @DisplayName("첫 페이지는 최신순이며 삭제된 댓글을 제외하고 size만큼 조회한다")
    void findByCursor_firstPage() throws InterruptedException {
        saveComment("1");
        Comment deleted = saveComment("2-삭제");
        saveComment("3");
        saveComment("4");

        deleted.softDelete();
        em.flush();
        em.clear();

        List<Comment> result = commentRepository.findByCursor(
            review.getId(),
            null,
            null,
            Sort.Direction.DESC,
            2
        );

        assertThat(result)
            .extracting(Comment::getContent)
            .containsExactly("4", "3");
    }

    @Test
    @DisplayName("다음 페이지는 after 이전에 생성된 댓글만 조회한다")
    void findByCursor_nextPage() throws InterruptedException {
        saveComment("1");
        saveComment("2");
        Comment c3 = saveComment("3");

        em.clear();

        List<Comment> result = commentRepository.findByCursor(
            review.getId(),
            c3.getCreatedAt(),
            c3.getId(),
            Sort.Direction.DESC,
            10
        );

        assertThat(result)
            .extracting(Comment::getContent)
            .containsExactly("2", "1");
    }

    @Test
    @DisplayName("오래된순(ASC) 첫 페이지는 오래된 댓글부터 조회한다")
    void findByCursor_asc_firstPage() throws InterruptedException {
        saveComment("1");
        saveComment("2");
        saveComment("3");

        em.clear();

        List<Comment> result = commentRepository.findByCursor(
            review.getId(),
            null,
            null,
            Sort.Direction.ASC,
            2
        );

        assertThat(result)
            .extracting(Comment::getContent)
            .containsExactly("1", "2");
    }

    @Test
    @DisplayName("오래된순(ASC) 다음 페이지는 after 이후에 생성된 댓글만 조회한다")
    void findByCursor_asc_nextPage() throws InterruptedException {
        Comment c1 = saveComment("1");
        saveComment("2");
        saveComment("3");

        em.clear();

        List<Comment> result = commentRepository.findByCursor(
            review.getId(),
            c1.getCreatedAt(),
            c1.getId(),
            Sort.Direction.ASC,
            10
        );

        assertThat(result)
            .extracting(Comment::getContent)
            .containsExactly("2", "3");
    }

    @Test
    @DisplayName("다른 리뷰의 댓글은 조회되지 않는다")
    void findByCursor_otherReview() throws InterruptedException {
        saveComment("이 리뷰의 댓글");

        // 기존 리뷰와 다른 책 생성
        Book anotherBook = Book.create(
            "다른 책 제목",
            user,
            "다른 저자",
            "다른 책 설명",
            "다른 출판사",
            LocalDate.of(2025, 1, 1),
            "9781234567806",
            null
        );

        ReflectionTestUtils.setField(anotherBook, "user", user);
        em.persist(anotherBook);

        // 같은 사용자가 다른 책에 작성한 리뷰 생성
        Review other = em.persist(
            Review.create(user, anotherBook, "다른 리뷰", 4)
        );

        em.persistAndFlush(
            Comment.create(other, user, "다른 리뷰의 댓글")
        );

        em.clear();

        // 원래 리뷰의 댓글만 조회되는지 확인
        List<Comment> result = commentRepository.findByCursor(
            review.getId(),
            null,
            null,
            Sort.Direction.DESC,
            10
        );

        assertThat(result)
            .extracting(Comment::getContent)
            .containsExactly("이 리뷰의 댓글");
    }

    @Test
    @DisplayName("논리 삭제된 댓글은 다음 페이지에서도 제외된다")
    void findByCursor_nextPage_excludesDeleted() throws InterruptedException {
        saveComment("1");
        Comment deleted = saveComment("2-삭제");
        Comment c3 = saveComment("3");

        deleted.softDelete();
        em.flush();
        em.clear();

        List<Comment> result = commentRepository.findByCursor(
            review.getId(),
            c3.getCreatedAt(),
            c3.getId(),
            Sort.Direction.DESC,
            10
        );

        assertThat(result)
            .extracting(Comment::getContent)
            .containsExactly("1");
    }

    @Test
    @DisplayName("createdAt이 같은 댓글이 페이지 경계에 걸려도 누락·중복 없이 조회된다")
    void cursorPagination_sameCreatedAt() throws InterruptedException {
        saveComment("1");
        saveComment("2");
        saveComment("3");

        em.flush();

        // createdAt을 동일하게 맞춤
        Instant sameTime = Instant.parse("2024-01-01T00:00:00Z");

        em.getEntityManager()
            .createNativeQuery("update comments set created_at = :t")
            .setParameter("t", sameTime)
            .executeUpdate();

        em.clear();

        // 첫 페이지 조회
        List<Comment> firstPage = commentRepository.findByCursor(
            review.getId(),
            null,
            null,
            Sort.Direction.DESC,
            2
        );

        // 마지막 요소를 커서로 다음 페이지 조회
        Comment last = firstPage.get(firstPage.size() - 1);

        List<Comment> nextPage = commentRepository.findByCursor(
            review.getId(),
            last.getCreatedAt(),
            last.getId(),
            Sort.Direction.DESC,
            10
        );

        List<Comment> all = new ArrayList<>(firstPage);
        all.addAll(nextPage);

        assertThat(firstPage).hasSize(2);
        assertThat(nextPage).hasSize(1);
        assertThat(all).extracting(Comment::getId).doesNotHaveDuplicates();
        assertThat(all)
            .extracting(Comment::getContent)
            .containsExactlyInAnyOrder("1", "2", "3");
    }

    @Test
    @DisplayName("삭제되지 않은 댓글 개수만 센다")
    void countByReviewIdAndIsDeletedFalse() throws InterruptedException {
        saveComment("1");
        Comment deleted = saveComment("2");

        deleted.softDelete();
        em.flush();

        assertThat(
            commentRepository.countByReviewIdAndIsDeletedFalse(review.getId())
        ).isEqualTo(1L);
    }

    @Test
    @DisplayName("물리 삭제하면 DB에서 행이 완전히 사라진다")
    void hardDelete_removesRow() throws InterruptedException {
        Comment comment = saveComment("삭제 대상");
        UUID id = comment.getId();

        commentRepository.delete(comment);
        em.flush();
        em.clear();

        assertThat(commentRepository.findById(id)).isEmpty();
        assertThat(em.find(Comment.class, id)).isNull();
        assertThat(commentRepository.countByReviewIdAndIsDeletedFalse(review.getId())).isZero();
    }

    @Test
    @DisplayName("논리 삭제는 행이 남고(isDeleted=true), 물리 삭제는 행이 사라진다")
    void softDelete_vs_hardDelete() throws InterruptedException {
        Comment soft = saveComment("논리 삭제 대상");
        Comment hard = saveComment("물리 삭제 대상");
        UUID softId = soft.getId();
        UUID hardId = hard.getId();

        soft.softDelete();
        commentRepository.delete(hard);
        em.flush();
        em.clear();

        // 논리 삭제: 행은 그대로 있고 플래그만 true, 일반 조회에서는 제외
        assertThat(commentRepository.findById(softId).orElseThrow().isDeleted()).isTrue();
        assertThat(commentRepository.findByIdAndIsDeletedFalse(softId)).isEmpty();
        // 물리 삭제: 행 자체가 없음
        assertThat(commentRepository.findById(hardId)).isEmpty();
    }

    @Test
    @DisplayName("논리 삭제된 댓글도 물리 삭제할 수 있다")
    void hardDelete_afterSoftDelete() throws InterruptedException {
        Comment comment = saveComment("논리 삭제 후 물리 삭제");
        UUID id = comment.getId();
        comment.softDelete();
        em.flush();

        commentRepository.delete(comment);
        em.flush();
        em.clear();

        assertThat(commentRepository.findById(id)).isEmpty();
    }
}
