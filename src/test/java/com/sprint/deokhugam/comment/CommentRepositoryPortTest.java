package com.sprint.deokhugam.comment;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import com.sprint.deokhugam.comment.application.port.out.CommentRepositoryPort;
import com.sprint.deokhugam.comment.domain.Comment;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.user.domain.User;

@DataJpaTest
class CommentRepositoryPortTest {

    @Autowired
    private CommentRepositoryPort commentRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private Review review;

    @BeforeEach
    void setUp() {
        user = em.persist(createUser());
        review = em.persist(createReview());
    }

    private User createUser() {
        // TODO: 프로젝트의 User 생성 방식에 맞게 작성
        throw new UnsupportedOperationException("User 생성 로직을 작성하세요");
    }

    private Review createReview() {
        // TODO: 프로젝트의 Review 생성 방식에 맞게 작성
        throw new UnsupportedOperationException("Review 생성 로직을 작성하세요");
    }

    private Comment saveComment(String content) throws InterruptedException {
        Comment comment = em.persistAndFlush(Comment.create(review, user, content));
        Thread.sleep(10); // createdAt 순서 구분용
        return comment;
    }

    @Test
    @DisplayName("논리 삭제되지 않은 댓글은 id로 조회된다")
    void findByIdAndIsDeletedFalse_found() throws InterruptedException {
        Comment comment = saveComment("댓글");

        assertThat(commentRepository.findByIdAndIsDeletedFalse(comment.getId())).isPresent();
    }

    @Test
    @DisplayName("논리 삭제된 댓글은 id로 조회되지 않는다")
    void findByIdAndIsDeletedFalse_deleted() throws InterruptedException {
        Comment comment = saveComment("댓글");
        comment.delete();
        em.flush();
        em.clear();

        assertThat(commentRepository.findByIdAndIsDeletedFalse(comment.getId())).isEmpty();
    }

    @Test
    @DisplayName("첫 페이지는 최신순이며 삭제된 댓글을 제외하고 size만큼 조회한다")
    void findFirstPage() throws InterruptedException {
        saveComment("1");
        Comment deleted = saveComment("2-삭제");
        saveComment("3");
        saveComment("4");
        deleted.delete();
        em.flush();
        em.clear();

        List<Comment> result = commentRepository.findFirstPage(review.getId(), PageRequest.of(0, 2));

        assertThat(result).extracting(Comment::getContent).containsExactly("4", "3");
    }

    @Test
    @DisplayName("다음 페이지는 after 이전에 생성된 댓글만 조회한다")
    void findNextPage() throws InterruptedException {
        Comment c1 = saveComment("1");
        Comment c2 = saveComment("2");
        Comment c3 = saveComment("3");
        em.clear();

        List<Comment> result = commentRepository.findNextPage(
            review.getId(), c3.getCreatedAt(), PageRequest.of(0, 10));

        assertThat(result).extracting(Comment::getContent).containsExactly("2", "1");
    }

    @Test
    @DisplayName("삭제되지 않은 댓글 개수만 센다")
    void countByReviewIdAndIsDeletedFalse() throws InterruptedException {
        saveComment("1");
        Comment deleted = saveComment("2");
        deleted.delete();
        em.flush();

        assertThat(commentRepository.countByReviewIdAndIsDeletedFalse(review.getId())).isEqualTo(1L);
    }
}
