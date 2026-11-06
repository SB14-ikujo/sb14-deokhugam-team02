package com.sprint.deokhugam.review.application.port.in;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.util.ReflectionTestUtils.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sprint.deokhugam.book.application.port.out.BookImageStorage;
import com.sprint.deokhugam.book.application.port.out.BookRepositoryPort;
import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.notification.application.port.in.NotificationUseCase;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewLikeDto;
import com.sprint.deokhugam.review.adapter.in.web.mapper.ReviewLikeMapper;
import com.sprint.deokhugam.review.application.port.out.ReviewLikeRepositoryPort;
import com.sprint.deokhugam.review.application.port.out.ReviewRepositoryPort;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.review.domain.ReviewLike;
import com.sprint.deokhugam.user.application.port.out.UserRepositoryPort;
import com.sprint.deokhugam.user.domain.User;

@ExtendWith(MockitoExtension.class)
class ReviewLikeServiceTest {

    @InjectMocks
    private ReviewLikeService reviewLikeService;

    @Mock
    private ReviewLikeMapper reviewLikeMapper;

    @Mock
    private NotificationUseCase notificationUseCase;

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private BookRepositoryPort bookRepository;

    @Mock
    private ReviewRepositoryPort reviewRepository;

    @Mock
    private BookImageStorage bookImageStorage;

    @Mock
    private ReviewLikeRepositoryPort reviewLikeRepository;

    private User createMockUser(String email, String nickname, String password) {
        User user = User.create(email, nickname, password);
        setField(user, "id", UUID.randomUUID());
        return user;
        // test@test.com, 수달짱, password1234!
    }

    private Book createMockBook(User user, String title, String author) {
        Book book = Book.create(
            title,
            user,
            author,
            "상세",
            "출판사",
            LocalDate.of(2026, 11, 1),
            "9788960773417",
            "url"
        );
        setField(book, "id", UUID.randomUUID());
        return book;
    }

    private Review createMockReview(User user, Book book) {
        return createMockReview(user, book, "재미없어요", 1);
    }

    private Review createMockReview(User user, Book book, String content, int rating) {
        Review review = Review.create(user, book, content, rating);
        setField(review, "id", UUID.randomUUID());
        return review;
    }

    @Test
    @DisplayName("좋아요 토글 성공: 이미 좋아요를 누른 상태라면 좋아요가 취소(삭제)된다.")
    void reviewLikeToggle_Success_Delete() {
        // given
        User mockUser = createMockUser("test@test.com", "수달", "password1234!");
        UUID requestUserId = mockUser.getId();

        Book mockBook = createMockBook(mockUser, "도서명", "작가");

        Review mockReview = createMockReview(mockUser, mockBook);
        UUID reviewId = mockReview.getId();

        ReviewLike mockReviewRike = ReviewLike.create(mockReview, mockUser);
        ReviewLikeDto mockResponseDto = new ReviewLikeDto(reviewId, requestUserId, false);

        given(userRepository.findByIdAndIsDeletedFalse(any()))
            .willReturn(Optional.of(mockUser));

        given(reviewLikeRepository.findByReviewIdAndUserId(any(), any()))
            .willReturn(Optional.of(mockReviewRike));

        given(reviewLikeMapper.toDto(any(), anyBoolean()))
            .willReturn(mockResponseDto);

        // when
        ReviewLikeDto result = reviewLikeService.reviewLikeToggle(reviewId, requestUserId);

        // then
        assertThat(result).isNotNull();
        verify(reviewLikeRepository).delete(any(ReviewLike.class));
        verify(reviewRepository).decreaseLikeCount(any());
    }

    @Test
    @DisplayName("좋아요 토글 성공: 좋아요를 누르지 않은 상태라면 새롭게 좋아요가 등록(저장)된다.")
    void reviewLikeToggle_Success_Save() {
        // given
        User mockUser = createMockUser("test@test.com", "수달", "password1234!");
        Book mockBook = createMockBook(mockUser, "도서명", "작가");
        Review mockReview = createMockReview(mockUser, mockBook);

        UUID requestUserId = mockUser.getId();
        UUID reviewId = mockReview.getId();

        ReviewLikeDto mockResponseDto = new ReviewLikeDto(reviewId, requestUserId, true);

        given(reviewLikeRepository.findByReviewIdAndUserId(reviewId, requestUserId)).willReturn(Optional.empty());
        given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(mockReview));
        given(userRepository.findByIdAndIsDeletedFalse(requestUserId)).willReturn(Optional.of(mockUser));
        given(reviewLikeMapper.toDto(any(ReviewLike.class), eq(true))).willReturn(mockResponseDto);

        // when
        ReviewLikeDto result = reviewLikeService.reviewLikeToggle(reviewId, requestUserId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.reviewId()).isEqualTo(reviewId);
        assertThat(result.userId()).isEqualTo(requestUserId);
        assertThat(result.liked()).isEqualTo(true);
        verify(reviewRepository).increaseLikeCount(reviewId);
        verify(notificationUseCase).createLikeNotification(eq(mockReview), anyString());
        verify(reviewLikeRepository).save(any(ReviewLike.class));
    }

}
