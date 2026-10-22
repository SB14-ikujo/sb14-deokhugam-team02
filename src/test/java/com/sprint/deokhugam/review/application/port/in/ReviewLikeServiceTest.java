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
        // 2. 가짜 객체를 만듦 (User, Book, Review)
        mockUser = User.create("test@naver.com", "강아지", "1234");

        mockBook = Book.create(
            "도서제목1",
            "작가1",
            "설명1",
            "퍼블리셔1",
            LocalDate.now(),
            "9788960773431",
            "https://example.com"
        );

        mockReview = Review.create(mockUser, mockBook, "리뷰 내용 테스트1", 5);

        // 1. 요청할 떄 이미 존재하는 유저, 리뷰의 ID로 요청을 보내기 때문에 있어야함
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
        ReviewLikeDto result = reviewLikeService.reviewLikeToggle(reviewId, userId);

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
        ReviewLikeDto result = reviewLikeService.reviewLikeToggle(reviewId, userId);

        // then
        verify(reviewCommandRepositoryPort).decreaseLikeCount(userId);
        verify(reviewLikeRepositoryPort).delete(mockLike);
        verify(reviewLikeRepositoryPort, never()).save(any());
    }

    // 실패 케이스
    @Test
    @DisplayName("존재하지 않는 리뷰 ID로 좋아요를 요청하면 예외(ReviewException)가 발생한다.")
    void toggleLike_shouldThrowException_whenReviewNotFound() {
        // given - 유저는 찾았지만, 리뷰 리포지토리에서 '없다(Optional.empty())'를 뱉도록 대본 작성
        given(userUseCase.findUserById(userId)).willReturn(mockUser);
        given(reviewCommandRepositoryPort.findById(reviewId)).willReturn(Optional.empty());

        // When & Then - assertThrows를 사용해 해당 서비스 로직이 예외를 진짜 터뜨리는지 검증
        assertThrows(ReviewException.class, () -> {
            reviewLikeService.reviewLikeToggle(reviewId, userId);
        });

        // 예외가 중간에 터졌으므로, 그 아래에 있는 좋아요 조회나 카운트 증감 로직은 절대 실행되면 안 됨!
        verify(reviewLikeRepositoryPort, never()).findByReviewAndUser(any(), any());
        verify(reviewCommandRepositoryPort, never()).increaseLikeCount(any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 좋아요를 요청하면 예외가 발생한다.")
    void toggleLike_shouldThrowException_whenUserNotFound() {
        // given - 유저 서비스에서 예외를 던지거나 null을 뱉도록 대본 작성
        // (보통 findUserById 내부에서 예외를 던지므로 런타임 예외 등으로 연출합니다)
        given(userUseCase.findUserById(userId)).willThrow(new RuntimeException("유저를 찾을 수 없습니다."));

        // When & Then - 유저 단계에서 바로 예외가 터지는지 확인
        assertThrows(RuntimeException.class, () -> {
            reviewLikeService.reviewLikeToggle(reviewId, userId);
        });

        // 유저 조회 실패로 뒤쪽 로직(리뷰 찾기 등)은 아예 실행 안 됨을 검증
        verify(reviewCommandRepositoryPort, never()).findById(any());
    }

    @Test
    @DisplayName("자신이 작성한 리뷰에 좋아요를 요청하는 경우를 검증한다.")
    void toggleLike_shouldHandle_whenUserLikesOwnReview() {
        // 💡 만약 현재 서비스 코드에 '본인 리뷰 좋아요 금지' 정책이 아직 없다면
        // 이 테스트는 정상적으로 좋아요 등록(성공 케이스와 동일) 처리가 될 것입니다.
        // 현재는 서비스 코드에 예외 처리가 없으므로, 정상 등록 흐름으로 검증문을 깔아둡니다.

        // given
        given(userUseCase.findUserById(userId)).willReturn(mockUser);
        given(reviewCommandRepositoryPort.findById(reviewId)).willReturn(Optional.of(mockReview));
        given(reviewLikeRepositoryPort.findByReviewAndUser(mockReview, mockUser)).willReturn(Optional.empty());

        ReviewLikeDto expectDto = reviewLikeMapper.toDto(any(), org.mockito.ArgumentMatchers.eq(true));
        given(reviewLikeMapper.toDto(any(), org.mockito.ArgumentMatchers.eq(true))).willReturn(expectDto);

        // when
        ReviewLikeDto result = reviewLikeService.reviewLikeToggle(reviewId, userId);

        // then
        // 본인 리뷰여도 현재 코드상으로는 그대로 좋아요가 등록됨을 검증
        verify(reviewLikeRepositoryPort).save(any(ReviewLike.class));
    }
}
