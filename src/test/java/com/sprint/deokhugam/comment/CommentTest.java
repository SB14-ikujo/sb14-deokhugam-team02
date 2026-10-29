package com.sprint.deokhugam.comment;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sprint.deokhugam.comment.domain.Comment;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.user.domain.User;

class CommentTest {

    private final Review review = mock(Review.class);
    private final User user = mock(User.class);

    @Test
    @DisplayName("댓글 생성 시 값이 설정되고 논리 삭제 상태는 false이다")
    void create() {
        Comment comment = Comment.create(review, user, "좋은 리뷰네요");

        assertThat(comment.getReview()).isSameAs(review);
        assertThat(comment.getUser()).isSameAs(user);
        assertThat(comment.getContent()).isEqualTo("좋은 리뷰네요");
        assertThat(comment.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("댓글 내용을 수정할 수 있다")
    void updateContent() {
        Comment comment = Comment.create(review, user, "수정 전");

        comment.updateContent("수정 후");

        assertThat(comment.getContent()).isEqualTo("수정 후");
    }

    @Test
    @DisplayName("논리 삭제하면 isDeleted가 true가 된다")
    void delete() {
        Comment comment = Comment.create(review, user, "삭제될 댓글");

        comment.softDelete();

        assertThat(comment.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("댓글 생성 시 비속어는 ***로 치환된다")
    void create_masksProfanity() {
        Comment comment = Comment.create(review, user, "이 시발 리뷰 별로");

        assertThat(comment.getContent()).isEqualTo("이 *** 리뷰 별로");
    }

    @Test
    @DisplayName("댓글 수정 시 비속어는 ***로 치환된다")
    void updateContent_masksProfanity() {
        Comment comment = Comment.create(review, user, "수정 전");

        comment.updateContent("개새끼 같은 리뷰");

        assertThat(comment.getContent()).isEqualTo("*** 같은 리뷰");
    }

}
