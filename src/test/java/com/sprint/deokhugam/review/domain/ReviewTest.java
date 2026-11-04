package com.sprint.deokhugam.review.domain;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.review.domain.exception.ReviewException;
import com.sprint.deokhugam.user.domain.User;

class ReviewTest {

    private User mockUser;
    private Book mockBook;
    private UUID userId;

    @BeforeEach
    void setUp() {
        mockUser = Mockito.mock(User.class);
        mockBook = Mockito.mock(Book.class);

        userId = UUID.randomUUID();
        given(mockUser.getId()).willReturn(userId);
    }

    @Test
    @DisplayName("리뷰 생성 시 전달한 데이터가 올바르게 세팅된다")
    void createReview_Success() {
        Review review = Review.create(mockUser, mockBook, "좋은 책이네요.", 5);

        assertThat(review.getUser()).isEqualTo(mockUser);
        assertThat(review.getBook()).isEqualTo(mockBook);
        assertThat(review.getContent()).isEqualTo("좋은 책이네요.");
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("본인이 작성한 리뷰라면 논리 삭제(softDelete)에 성공한다")
    void softDelete_Success() {
        Review review = Review.create(mockUser, mockBook, "좋은 책이네요.", 5);

        review.softDelete(userId);

        assertThat(review.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("이미 삭제된 리뷰를 다시 삭제하려고 하면 예외가 발생한다")
    void softDelete_Fail_AlreadyDeleted() {
        Review review = Review.create(mockUser, mockBook, "좋은 책이네요.", 5);
        review.softDelete(userId);

        assertThatThrownBy(() -> review.softDelete(userId))
            .isInstanceOf(ReviewException.class);
    }

    @Test
    @DisplayName("리뷰 작성자가 아닌 다른 유저가 삭제하려고 하면 예외가 발생한다")
    void softDelete_Fail_Forbidden() {
        Review review = Review.create(mockUser, mockBook, "좋은 책이네요.", 5);
        UUID strangerId = UUID.randomUUID();

        assertThatThrownBy(() -> review.softDelete(strangerId))
            .isInstanceOf(ReviewException.class);
    }

    @Test
    @DisplayName("올바른 내용과 평점으로 수정하면 정상적으로 변경된다")
    void update_Success() {
        Review review = Review.create(mockUser, mockBook, "이전 내용", 3);

        review.update("수정된 내용", 5);

        assertThat(review.getContent()).isEqualTo("수정된 내용");
        assertThat(review.getRating()).isEqualTo(5);
    }

    @Test
    @DisplayName("수정할 내용이 null이거나 공백이면 예외가 발생한다")
    void update_Fail_InvalidContent() {
        Review review = Review.create(mockUser, mockBook, "이전 내용", 3);

        assertThatThrownBy(() -> review.update(null, 5))
            .isInstanceOf(ReviewException.class);

        assertThatThrownBy(() -> review.update("   ", 5))
            .isInstanceOf(ReviewException.class);
    }

    @Test
    @DisplayName("수정할 평점이 null이거나 범위를 벗어나면 예외가 발생한다")
    void update_Fail_InvalidRating() {
        Review review = Review.create(mockUser, mockBook, "이전 내용", 3);

        assertThatThrownBy(() -> review.update("수정된 내용", null))
            .isInstanceOf(ReviewException.class);

        assertThatThrownBy(() -> review.update("수정된 내용", 0))
            .isInstanceOf(ReviewException.class);

        assertThatThrownBy(() -> review.update("수정된 내용", 6))
            .isInstanceOf(ReviewException.class);
    }
}
