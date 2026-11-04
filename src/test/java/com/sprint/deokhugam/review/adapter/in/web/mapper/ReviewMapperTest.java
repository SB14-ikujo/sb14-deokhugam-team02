package com.sprint.deokhugam.review.adapter.in.web.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewDto;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.user.domain.User;

class ReviewMapperTest {

    private final ReviewMapper mapper = Mappers.getMapper(ReviewMapper.class);

    @Test
    @DisplayName("맵퍼 정상 동작 테스트")
    void toDto_Success_Test() {
        // given
        Review mockReview = Mockito.mock(Review.class);
        User mockUser = Mockito.mock(User.class);
        Book mockBook = Mockito.mock(Book.class);

        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        // given
        given(mockReview.getUser()).willReturn(mockUser);
        given(mockReview.getBook()).willReturn(mockBook);

        given(mockUser.getId()).willReturn(userId);
        given(mockUser.getNickname()).willReturn("수달짱");

        given(mockBook.getId()).willReturn(bookId);
        given(mockBook.getTitle()).willReturn("스프링 부트 정복기");
        given(mockBook.getThumbnailUrl()).willReturn("https://example.com");

        given(mockReview.getContent()).willReturn("재밌네요.");
        given(mockReview.getRating()).willReturn(5);

        // when
        ReviewDto dto = mapper.toDto(mockReview, true);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.userId()).isEqualTo(userId);
        assertThat(dto.userNickname()).isEqualTo("수달짱");
        assertThat(dto.bookId()).isEqualTo(bookId);
        assertThat(dto.bookTitle()).isEqualTo("스프링 부트 정복기");
        assertThat(dto.likedByMe()).isTrue();
    }

    @Test
    @DisplayName("맵퍼 Null 및 누락 입력 테스트")
    void toDto_Null_Branch_Test() {
        // given
        Review reviewWithNullUserAndBook = Mockito.mock(Review.class);
        given(reviewWithNullUserAndBook.getUser()).willReturn(null);
        given(reviewWithNullUserAndBook.getBook()).willReturn(null);

        Review reviewWithNullFields = Mockito.mock(Review.class);
        User userWithNullId = Mockito.mock(User.class);
        Book bookWithNullFields = Mockito.mock(Book.class);
        given(reviewWithNullFields.getUser()).willReturn(userWithNullId);
        given(reviewWithNullFields.getBook()).willReturn(bookWithNullFields);

        // when & then
        assertThat(mapper.toDto(null, false)).isNull();

        ReviewDto dto1 = mapper.toDto(reviewWithNullUserAndBook, false);
        assertThat(dto1).isNotNull();
        assertThat(dto1.userId()).isNull();
        assertThat(dto1.bookId()).isNull();

        ReviewDto dto2 = mapper.toDto(reviewWithNullFields, false);
        assertThat(dto2).isNotNull();
        assertThat(dto2.userId()).isNull();
        assertThat(dto2.userNickname()).isNull();
    }
}
