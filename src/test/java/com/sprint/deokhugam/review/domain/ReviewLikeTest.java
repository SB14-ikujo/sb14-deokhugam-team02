package com.sprint.deokhugam.review.domain;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.sprint.deokhugam.user.domain.User;

class ReviewLikeTest {

    @Test
    @DisplayName("리뷰 좋아요 생성 시 전달한 리뷰와 유저 데이터가 올바르게 세팅된다")
    void createReviewLike_Success() {
        // given
        Review mockReview = Mockito.mock(Review.class);
        User mockUser = Mockito.mock(User.class);

        // when
        ReviewLike reviewLike = ReviewLike.create(mockReview, mockUser);

        // then
        assertThat(reviewLike.getReview()).isEqualTo(mockReview);
        assertThat(reviewLike.getUser()).isEqualTo(mockUser);
    }
}
