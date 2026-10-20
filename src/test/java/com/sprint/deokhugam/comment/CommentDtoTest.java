package com.sprint.deokhugam.comment;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentDto;
import com.sprint.deokhugam.comment.domain.Comment;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.user.domain.User;

class CommentDtoTest {

    @Test
    @DisplayName("Comment 엔티티를 CommentDto로 변환한다")
    void from() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Review review = mock(Review.class);
        User user = mock(User.class);
        when(review.getId()).thenReturn(reviewId);
        when(user.getId()).thenReturn(userId);
        when(user.getNickname()).thenReturn("덕후");

        Comment comment = Comment.create(review, user, "댓글 내용");

        CommentDto dto = CommentDto.from(comment);

        assertThat(dto.reviewId()).isEqualTo(reviewId);
        assertThat(dto.userId()).isEqualTo(userId);
        assertThat(dto.userNickname()).isEqualTo("덕후");
        assertThat(dto.content()).isEqualTo("댓글 내용");
    }
}
