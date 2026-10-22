package com.sprint.deokhugam.review.application.port.in;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.notification.application.port.in.NotificationUseCase;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewLikeDto;
import com.sprint.deokhugam.review.adapter.in.web.mapper.ReviewLikeMapper;
import com.sprint.deokhugam.review.application.port.out.ReviewCommandRepositoryPort;
import com.sprint.deokhugam.review.application.port.out.ReviewLikeRepositoryPort;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.review.domain.ReviewLike;
import com.sprint.deokhugam.review.domain.exception.ReviewException;
import com.sprint.deokhugam.user.application.port.in.UserUseCase;
import com.sprint.deokhugam.user.domain.User;

@ExtendWith(MockitoExtension.class)
public class ReviewLikeServiceTest {
    @Mock
    private ReviewLikeMapper reviewLikeMapper;
    @Mock
    private NotificationUseCase notificationUseCase;
    @Mock
    private UserUseCase userUseCase;
    @Mock
    private ReviewCommandRepositoryPort reviewCommandRepositoryPort;
    @Mock
    private ReviewLikeRepositoryPort reviewLikeRepositoryPort;
    @InjectMocks
    private ReviewLikeService reviewLikeService;

    // 사전 준비
    private UUID reviewId;
    private UUID userId;
    private User mockUser;
    private Book mockBook;
    private Review mockReview;

    @BeforeEach
    void setUp() {
        // 가짜 유저(A) 객체 생성
        mockUser = User.create("test@naver.com", "강아지", "1234");

        // A가 등록한 가짜 책 객체 생성
        mockBook = Book.create(
            "도서제목1",
            "작가1",
            "설명1",
            "퍼블리셔1",
            LocalDate.now(),
            "9788960773431",
            "https://example.com"
        );

        // A
        mockReview = Review.create(mockUser, mockBook, "리뷰 내용 테스트1", 5);

        userId = mockUser.getId();
        reviewId = mockReview.getId();
    }

    // 성공 케이스
    @Test
    @DisplayName("처음으로 좋아요 요청을 하면 좋아요 데이터가 저장되고 리뷰의 좋아요 수가 1 증가한다.")
    void toggleLike_shouldCreateLike_whenLikeDoesNotExist() {
        // given
        given(userUseCase.findUserById(userId)).willReturn(mockUser);
        given(reviewCommandRepositoryPort.findById(reviewId)).willReturn(Optional.of(mockReview));
        given(reviewLikeRepositoryPort.findByReviewAndUser(mockReview, mockUser)).willReturn(Optional.empty());

        ReviewLikeDto expectDto = reviewLikeMapper.toDto(any(), org.mockito.ArgumentMatchers.eq(true));
        given(reviewLikeMapper.toDto(any(), org.mockito.ArgumentMatchers.eq(true))).willReturn(expectDto);

        // when
        ReviewLikeDto result = reviewLikeService.reviewLikeToggle(reviewId);

        // then
        verify(reviewCommandRepositoryPort).increaseLikeCount(userId);
        verify(notificationUseCase).createLikeNotification(mockReview, "강아지");
        verify(reviewLikeRepositoryPort).save(any(ReviewLike.class));
        verify(reviewCommandRepositoryPort, never()).decreaseLikeCount(any());
    }

    @Test
    @DisplayName("이미 좋아요를 누른 상태에서 다시 요청하면 좋아요 데이터가 삭제되고 리뷰의 좋아요 수가 1 감소한다.")
    void toggleLike_shouldDeleteLike_whenLikeAlreadyExists() {
        // given
        given(userUseCase.findUserById(userId)).willReturn(mockUser);
        given(reviewCommandRepositoryPort.findById(reviewId)).willReturn(Optional.of(mockReview));

        ReviewLike mockLike = ReviewLike.create(mockReview, mockUser);
        given(reviewLikeRepositoryPort.findByReviewAndUser(mockReview, mockUser)).willReturn(Optional.of(mockLike));

        ReviewLikeDto expectDto = reviewLikeMapper.toDto(any(), org.mockito.ArgumentMatchers.eq(false));
        given(reviewLikeMapper.toDto(any(), org.mockito.ArgumentMatchers.eq(false))).willReturn(expectDto);

        // when
        ReviewLikeDto result = reviewLikeService.reviewLikeToggle(reviewId);

        // then
        verify(reviewCommandRepositoryPort).decreaseLikeCount(userId);
        verify(reviewLikeRepositoryPort).delete(mockLike);
        verify(reviewLikeRepositoryPort, never()).save(any());
    }

    // 실패 케이스
    @Test
    @DisplayName("존재하지 않는 리뷰 ID로 좋아요를 요청하면 예외(ReviewException)가 발생한다.")
    void toggleLike_shouldThrowException_whenReviewNotFound() {
        // given
        given(userUseCase.findUserById(userId)).willReturn(mockUser);
        given(reviewCommandRepositoryPort.findById(reviewId)).willReturn(Optional.empty());

        // When & Then
        assertThrows(ReviewException.class, () -> {
            reviewLikeService.reviewLikeToggle(reviewId);
        });
        verify(reviewLikeRepositoryPort, never()).findByReviewAndUser(any(), any());
        verify(reviewCommandRepositoryPort, never()).increaseLikeCount(any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 좋아요를 요청하면 예외가 발생한다.")
    void toggleLike_shouldThrowException_whenUserNotFound() {
        // given
        given(userUseCase.findUserById(userId)).willThrow(new RuntimeException("유저를 찾을 수 없습니다."));

        // When & Then - 유저 단계에서 바로 예외가 터지는지 확인
        assertThrows(RuntimeException.class, () -> {
            reviewLikeService.reviewLikeToggle(reviewId);
        });

        verify(reviewCommandRepositoryPort, never()).findById(any());
    }

    @Test
    @DisplayName("자신이 작성한 리뷰에 좋아요를 요청하는 경우를 검증한다.")
    void toggleLike_shouldHandle_whenUserLikesOwnReview() {

        // given
        given(userUseCase.findUserById(userId)).willReturn(mockUser);
        given(reviewCommandRepositoryPort.findById(reviewId)).willReturn(Optional.of(mockReview));
        given(reviewLikeRepositoryPort.findByReviewAndUser(mockReview, mockUser)).willReturn(Optional.empty());

        ReviewLikeDto expectDto = reviewLikeMapper.toDto(any(), org.mockito.ArgumentMatchers.eq(true));
        given(reviewLikeMapper.toDto(any(), org.mockito.ArgumentMatchers.eq(true))).willReturn(expectDto);

        // when
        ReviewLikeDto result = reviewLikeService.reviewLikeToggle(reviewId);

        // then
        verify(reviewLikeRepositoryPort).save(any(ReviewLike.class));
    }
}
