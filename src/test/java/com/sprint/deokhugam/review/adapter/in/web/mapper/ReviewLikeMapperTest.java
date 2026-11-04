package com.sprint.deokhugam.review.adapter.in.web.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewLikeDto;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.review.domain.ReviewLike;
import com.sprint.deokhugam.user.domain.User;

class ReviewLikeMapperTest {

    private final ReviewLikeMapper mapper = Mappers.getMapper(ReviewLikeMapper.class);

    @Test
    @DisplayName("리뷰 좋아요 맵퍼 정상 테스트")
    void toDto_Success_Test() {
        // given
        ReviewLike mockReviewLike = Mockito.mock(ReviewLike.class);
        Review mockReview = Mockito.mock(Review.class);
        User mockUser = Mockito.mock(User.class);

        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(mockReviewLike.getReview()).willReturn(mockReview);
        given(mockReviewLike.getUser()).willReturn(mockUser);

        given(mockReview.getId()).willReturn(reviewId);
        given(mockUser.getId()).willReturn(userId);

        // when
        ReviewLikeDto dto = mapper.toDto(mockReviewLike, true);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.reviewId()).isEqualTo(reviewId);
        assertThat(dto.userId()).isEqualTo(userId);
        assertThat(dto.liked()).isTrue();
    }

    @Test
    @DisplayName("리뷰 좋아요 맵퍼 Null 테스트")
    void toDto_Null_Branch_Test() {
        // given
        ReviewLike likeWithNullReviewAndUser = Mockito.mock(ReviewLike.class);
        given(likeWithNullReviewAndUser.getReview()).willReturn(null);
        given(likeWithNullReviewAndUser.getUser()).willReturn(null);

        ReviewLike likeWithNullIds = Mockito.mock(ReviewLike.class);
        Review reviewWithNullId = Mockito.mock(Review.class);
        User userWithNullId = Mockito.mock(User.class);
        given(likeWithNullIds.getReview()).willReturn(reviewWithNullId);
        given(likeWithNullIds.getUser()).willReturn(userWithNullId);

        // when & then
        assertThat(mapper.toDto(null, false)).isNull();

        ReviewLikeDto dto1 = mapper.toDto(likeWithNullReviewAndUser, false);
        assertThat(dto1).isNotNull();
        assertThat(dto1.reviewId()).isNull();
        assertThat(dto1.userId()).isNull();

        ReviewLikeDto dto2 = mapper.toDto(likeWithNullIds, false);
        assertThat(dto2).isNotNull();
        assertThat(dto2.reviewId()).isNull();
        assertThat(dto2.userId()).isNull();
    }
}
