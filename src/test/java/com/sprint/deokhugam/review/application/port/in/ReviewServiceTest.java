package com.sprint.deokhugam.review.application.port.in;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.util.ReflectionTestUtils.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.sprint.deokhugam.book.application.port.out.BookImageStorage;
import com.sprint.deokhugam.book.application.port.out.BookRepositoryPort;
import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewCreateRequest;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewDto;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewListRequest;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewUpdateRequest;
import com.sprint.deokhugam.review.adapter.in.web.mapper.ReviewMapper;
import com.sprint.deokhugam.review.application.port.out.ReviewLikeRepositoryPort;
import com.sprint.deokhugam.review.application.port.out.ReviewRepositoryPort;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.review.domain.exception.ReviewException;
import com.sprint.deokhugam.user.application.port.out.UserRepositoryPort;
import com.sprint.deokhugam.user.domain.User;
import com.sprint.deokhugam.user.domain.exception.UserException;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepositoryPort reviewRepository;

    @Mock
    private ReviewLikeRepositoryPort reviewLikeRepository;

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private BookRepositoryPort bookRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private BookImageStorage bookImageStorage; // 💡 상단 의존성 구역에 이 가짜 목 객체를 꼭 선언해 주세요!

    private ReviewDto createMockReviewDto(Review mockReview, Book mockBook, User mockUser) {
        return new ReviewDto(
            mockReview.getId(),
            mockBook.getId(),
            mockBook.getTitle(),
            mockBook.getThumbnailUrl(),
            mockUser.getId(),
            mockUser.getNickname(),
            mockReview.getContent(),
            mockReview.getRating(),
            0,
            0,
            false,
            Instant.now(),
            Instant.now()
        );
    }

    private ReviewDto updateMockReviewDto(Review mockReview, Book mockBook, User mockUser, String content,
        Integer rating) {
        return new ReviewDto(
            mockReview.getId(),
            mockBook.getId(),
            mockBook.getTitle(),
            mockBook.getThumbnailUrl(),
            mockUser.getId(),
            mockUser.getNickname(),
            content,
            rating,
            0,
            0,
            false,
            Instant.now(),
            Instant.now()
        );
    }

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

    // < CREATE >
    @Test
    @DisplayName("리뷰 생성 성공: 검증을 모두 통과하면 도서의 리뷰 수가 증가하고 정상 등록된다.")
    void createReview_Success() {
        // given
        User mockUser = createMockUser("test@test.com", "수달짱", "password1234!");
        UUID userId = mockUser.getId();

        Book mockBook = createMockBook(mockUser, "도서명", "작가");
        UUID bookId = mockBook.getId();

        ReviewCreateRequest createRequest = new ReviewCreateRequest(bookId, userId, "재밌네요.", 5);
        Review mockReview = createMockReview(mockUser, mockBook, "재밌네요.", 5);
        ReviewDto mockReviewDto = createMockReviewDto(mockReview, mockBook, mockUser);

        given(userRepository.findByIdAndIsDeletedFalseForUpdate(userId)).willReturn(Optional.of(mockUser));
        given(bookRepository.findByIdAndIsDeletedFalse(bookId)).willReturn(Optional.of(mockBook));
        given(reviewRepository.findValidReview(userId, bookId)).willReturn(Optional.empty());

        given(bookRepository.addReview(any(), anyInt())).willReturn(1);
        given(reviewRepository.save(any(Review.class))).willReturn(mockReview);
        given(reviewMapper.toDto(any(Review.class), eq(false), any())).willReturn(mockReviewDto);

        // when
        ReviewDto result = reviewService.createReview(createRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo(mockReviewDto.content());
        assertThat(result.rating()).isEqualTo(mockReviewDto.rating());

        // 리뷰 수/평점은 엔티티가 아니라 DB UPDATE 쿼리로 갱신된다
        verify(bookRepository).addReview(bookId, 5);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 생성 실패: 요청한 유저가 존재하지 않거나 이미 삭제된 유저이면 등록할 수 없다.")
    void createReview_Fail_NotFound_User() {
        // given
        User mockUser = createMockUser("test@test.com", "수달", "password1234!");
        Book mockBook = createMockBook(mockUser, "도서명", "작가");
        UUID bookId = mockBook.getId();

        UUID nonExistUserId = UUID.randomUUID();

        ReviewCreateRequest failRequest = new ReviewCreateRequest(bookId, nonExistUserId, "재밌어요", 5);

        given(userRepository.findByIdAndIsDeletedFalseForUpdate(failRequest.userId())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(failRequest)).isInstanceOf(UserException.class);
    }

    @Test
    @DisplayName("리뷰 생성 실패: 이미 해당 도서에 리뷰를 작성한 유저는 추가로 등록할 수 없다.")
    void createReview_Fail_AlreadyExists() {
        // given
        User mockUser = createMockUser("test@test.com", "수달짱", "password1234!");
        Book mockBook = createMockBook(mockUser, "도서명", "작가");
        Review existingReview = createMockReview(mockUser, mockBook, "이미 작성한 첫 번째 리뷰", 4);

        UUID userId = mockUser.getId();
        UUID bookId = mockBook.getId();

        ReviewCreateRequest createRequest = new ReviewCreateRequest(bookId, userId, "두 번째 리뷰 시도", 5);
        UUID realUserId = createRequest.userId();
        UUID realBookId = createRequest.bookId();

        given(userRepository.findByIdAndIsDeletedFalseForUpdate(realUserId)).willReturn(Optional.of(mockUser));
        given(bookRepository.findByIdAndIsDeletedFalse(realBookId)).willReturn(Optional.of(mockBook));
        given(reviewRepository.findValidReview(realUserId, realBookId)).willReturn(Optional.of(existingReview));

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(createRequest))
            .isInstanceOf(ReviewException.class);
    }

    // < READ  >
    @Test
    @DisplayName("리뷰 상세조회 성공: 존재하는 리뷰 ID로 조회하면 정상적인 DTO를 반환한다.")
    void getReviewDetail_Success() {
        // given
        User mockUser = createMockUser("test@test.com", "수달짱", "password1234!");
        UUID userId = mockUser.getId();

        Book mockBook = createMockBook(mockUser, "도서명", "작가");
        UUID bookId = mockBook.getId();

        Review mockReview = createMockReview(mockUser, mockBook); // 기본값: 재미없어요, 1
        UUID reviewId = mockReview.getId();

        UUID requestUserId = mockUser.getId();

        ReviewDto mockReviewDto = createMockReviewDto(mockReview, mockBook, mockUser);

        given(userRepository.findByIdAndIsDeletedFalse(requestUserId)).willReturn(Optional.of(mockUser));
        given(reviewRepository.findByFetchJoin(reviewId)).willReturn(Optional.of(mockReview));
        given(reviewLikeRepository.existsByReviewIdAndUserId(reviewId, requestUserId)).willReturn(false);
        given(bookImageStorage.getImageUrl(any(), any())).willReturn("https://example.com");
        given(reviewMapper.toDto(eq(mockReview), eq(false), any())).willReturn(mockReviewDto);

        // when
        ReviewDto result = reviewService.getReviewDetail(reviewId, requestUserId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("재미없어요");
        assertThat(result.rating()).isEqualTo(1);

        verify(userRepository).findByIdAndIsDeletedFalse(requestUserId);
        verify(reviewRepository).findByFetchJoin(reviewId);
        verify(reviewLikeRepository).existsByReviewIdAndUserId(reviewId, requestUserId);
    }

    @Test
    @DisplayName("리뷰 목록조회 성공: 최신순(createdAt) 정렬 시 커서는 null, after에는 마지막 리뷰의 생성시간이 정확히 담긴다.")
    void getReviewList_Success_OrderByCreatedAt() {
        // given
        User mockUser1 = createMockUser("user1@test.com", "수달짱", "pass1234!");
        User mockUser2 = createMockUser("user2@test.com", "홍길동", "pass5678!");
        Book mockBook = createMockBook(mockUser1, "도서명", "작가");

        Review review1 = createMockReview(mockUser1, mockBook, "첫 번째 리뷰", 5);
        Review review2 = createMockReview(mockUser2, mockBook, "두 번째 리뷰", 4);
        List<Review> mockReviewEntities = List.of(review1, review2);

        Instant lastReviewCreatedAt = Instant.parse("2026-11-03T12:00:00Z");

        ReviewDto dto1 = new ReviewDto(review1.getId(), mockBook.getId(), "도서명", "url", mockUser1.getId(), "수달짱",
            "첫 번째 리뷰", 5, 0, 0, false, lastReviewCreatedAt, lastReviewCreatedAt);
        ReviewDto dto2 = new ReviewDto(review2.getId(), mockBook.getId(), "도서명", "url", mockUser2.getId(), "홍길동",
            "두 번째 리뷰", 4, 0, 0, false, lastReviewCreatedAt, lastReviewCreatedAt);

        ReviewListRequest request = new ReviewListRequest(
            mockUser1.getId(), mockBook.getId(), "리뷰", "createdAt", Sort.Direction.DESC,
            null, lastReviewCreatedAt, 1
        );
        UUID requestUserId = mockUser1.getId();

        given(bookRepository.findByIdAndIsDeletedFalse(mockBook.getId())).willReturn(Optional.of(mockBook));
        given(userRepository.findByIdAndIsDeletedFalse(mockUser1.getId())).willReturn(Optional.of(mockUser1));
        given(reviewRepository.getReviewList(any(), any(), any(), any(), any(), any(), any(), anyInt())).willReturn(
            mockReviewEntities);

        List<UUID> mockLikedReviewIds = new java.util.ArrayList<>();
        given(reviewLikeRepository.findLikeReviewIds(any(), any())).willReturn(mockLikedReviewIds);
        given(bookImageStorage.getImageUrl(any(), any())).willReturn("https://example.com");
        given(reviewMapper.toDto(eq(review1), anyBoolean(), any())).willReturn(dto1);
        given(reviewMapper.toDto(eq(review2), anyBoolean(), any())).willReturn(dto2);
        given(reviewRepository.countByConditions(any(), any(), any())).willReturn(2L);
        // when
        CursorPageResponse<ReviewDto> result = reviewService.getReviewList(request, requestUserId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.totalElements()).isEqualTo(2L);
        assertThat(result.content().size()).isEqualTo(1);

        assertThat(result.nextCursor()).isNull();
        assertThat(result.nextAfter()).isEqualTo(lastReviewCreatedAt);
    }

    @Test
    @DisplayName("리뷰 목록조회 성공: 평점순(rating) 정렬 시 커서에는 마지막 평점 문자열, after에는 마지막 리뷰의 생성시간이 정확히 담긴다.")
    void getReviewList_Success_OrderByRating() {
        // given
        User mockUser1 = createMockUser("user1@test.com", "수달짱", "pass1234!");
        User mockUser2 = createMockUser("user2@test.com", "홍길동", "pass5678!");
        Book mockBook = createMockBook(mockUser1, "도서명", "작가");

        Review review1 = createMockReview(mockUser1, mockBook, "첫 번째 리뷰", 5);
        Review review2 = createMockReview(mockUser2, mockBook, "두 번째 리뷰", 4);
        List<Review> mockReviewEntities = List.of(review1, review2);

        Instant lastReviewCreatedAt = Instant.parse("2026-11-03T12:00:00Z");
        ReviewDto dto1 = new ReviewDto(review1.getId(), mockBook.getId(), "도서명", "url", mockUser1.getId(), "수달짱",
            "첫 번째 리뷰", 5, 0, 0, false, lastReviewCreatedAt, lastReviewCreatedAt);
        ReviewDto dto2 = new ReviewDto(review2.getId(), mockBook.getId(), "도서명", "url", mockUser2.getId(), "홍길동",
            "두 번째 리뷰", 4, 0, 0, false, lastReviewCreatedAt, lastReviewCreatedAt);

        ReviewListRequest request = new ReviewListRequest(
            mockUser1.getId(), mockBook.getId(), "리뷰", "rating", Sort.Direction.DESC,
            null, null, 1
        );
        UUID requestUserId = mockUser1.getId();

        given(bookRepository.findByIdAndIsDeletedFalse(mockBook.getId())).willReturn(Optional.of(mockBook));
        given(userRepository.findByIdAndIsDeletedFalse(mockUser1.getId())).willReturn(Optional.of(mockUser1));
        given(reviewRepository.getReviewList(any(), any(), any(), any(), any(), any(), any(), anyInt())).willReturn(
            mockReviewEntities);

        given(bookImageStorage.getImageUrl(any(), any())).willReturn("https://example.com");
        given(reviewMapper.toDto(eq(review1), anyBoolean(), any())).willReturn(dto1);
        given(reviewMapper.toDto(eq(review2), anyBoolean(), any())).willReturn(dto2);
        given(reviewRepository.countByConditions(any(), any(), any())).willReturn(2L);

        // when
        CursorPageResponse<ReviewDto> result = reviewService.getReviewList(request, requestUserId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.totalElements()).isEqualTo(2L);
        assertThat(result.content().size()).isEqualTo(1);
        assertThat(result.nextCursor()).isEqualTo("5.0");
        assertThat(result.nextAfter()).isEqualTo(lastReviewCreatedAt);
    }

    @Test
    @DisplayName("리뷰 상세조회 실패: 존재하지 않거나 삭제된 유저 ID로 요청하면 UserException이 발생한다.")
    void getReviewDetail_Fail_UserNotFound() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID nonUserId = UUID.randomUUID();

        given(userRepository.findByIdAndIsDeletedFalse(nonUserId)).willReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> reviewService.getReviewDetail(reviewId, nonUserId))
            .isInstanceOf(UserException.class);

        // then
        verify(userRepository).findByIdAndIsDeletedFalse(nonUserId);
        verifyNoInteractions(reviewRepository, reviewLikeRepository, reviewMapper);
    }

    @Test
    @DisplayName("리뷰 상세조회 실패: 존재하지 않는 리뷰 IDㄹ 조회하면 ReviewException이 발생한다.")
    void getReviewDetail_Fail_NotFound() {
        // given
        UUID reviewId = UUID.randomUUID();
        User mockUser = createMockUser("test@test.com", "수달짱", "password1234!");
        UUID userId = mockUser.getId();

        given(userRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(mockUser));
        given(reviewRepository.findByFetchJoin(reviewId)).willReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> reviewService.getReviewDetail(reviewId, userId))
            .isInstanceOf(ReviewException.class);

        // then
        verify(userRepository).findByIdAndIsDeletedFalse(userId);
        verify(reviewRepository).findByFetchJoin(reviewId);

        verifyNoInteractions(reviewLikeRepository, reviewMapper);
    }

    @Test
    @DisplayName("리뷰 목록조회 실패: 평점순 정렬일 때 커서가 숫자가 아닌 형식이면 예외가 발생한다.")
    void getReviewList_Fail_InvalidCursor_Format() {
        // given
        UUID requestUserId = UUID.randomUUID();
        ReviewListRequest request = new ReviewListRequest(
            null, null, null, "rating", Sort.Direction.DESC, "not-a-number", null, 10
        );

        // when & then
        assertThatThrownBy(() -> reviewService.getReviewList(request, requestUserId))
            .isInstanceOf(ReviewException.class)
            .hasMessageContaining("올바르지 않은 리뷰 평점 점수입니다.");
    }

    @Test
    @DisplayName("리뷰 목록조회 실패: 평점순 정렬일 때 커서가 1.0 미만(0.9)이면 예외가 발생한다.")
    void getReviewList_Fail_CursorLessThanOne() {
        // given
        UUID requestUserId = UUID.randomUUID();
        ReviewListRequest request = new ReviewListRequest(
            null, null, null, "rating", Sort.Direction.DESC, "0.9", null, 10
        );

        // when & then
        assertThatThrownBy(() -> reviewService.getReviewList(request, requestUserId))
            .isInstanceOf(ReviewException.class)
            .hasMessageContaining("올바르지 않은 리뷰 평점 점수입니다.");
    }

    @Test
    @DisplayName("리뷰 목록조회 실패: 평점순 정렬일 때 커서가 5.0 초과(5.1)면 예외가 발생한다.")
    void getReviewList_Fail_CursorGreaterThanFive() {
        // given
        UUID requestUserId = UUID.randomUUID();
        ReviewListRequest request = new ReviewListRequest(
            null, null, null, "rating", Sort.Direction.DESC, "5.1", null, 10
        );

        // when & then
        assertThatThrownBy(() -> reviewService.getReviewList(request, requestUserId))
            .isInstanceOf(ReviewException.class)
            .hasMessageContaining("올바르지 않은 리뷰 평점 점수입니다.");
    }

    // < UPDATE >
    @Test
    @DisplayName("리뷰 수정 성공: 권한이 있는 유저가 올바른 내용을 요청하면 리뷰 내용과 별점이 변경된다.")
    void updateReview_Success() {
        // given
        User author = User.create("test@test.com", "수달짱", "password1234!");
        UUID userId = UUID.randomUUID();
        setField(author, "id", userId);

        Book realBook = Book.create(
            "도서명", author, "작가", "상세", "출판사",
            LocalDate.of(2026, 11, 1), "9788960773417", "https://example.com"
        );
        setField(realBook, "id", UUID.randomUUID());

        Review realReview = Review.create(author, realBook, "재미없어요", 1);
        UUID reviewId = UUID.randomUUID();
        setField(realReview, "id", reviewId);

        ReviewUpdateRequest updateRequest = new ReviewUpdateRequest("완전 명작이네요!", 5);

        ReviewDto mockReviewDto = new ReviewDto(
            reviewId, realBook.getId(), "도서명", "https://example.com", userId, "수달짱",
            "완전 명작이네요!", 5, 0, 0, false, Instant.now(), Instant.now()
        );

        given(reviewRepository.findByFetchJoin(reviewId)).willReturn(Optional.of(realReview));
        given(reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId)).willReturn(true);
        given(bookRepository.changeReviewRating(any(), anyInt(), anyInt())).willReturn(1);
        given(reviewMapper.toDto(any(), anyBoolean(), any())).willReturn(mockReviewDto);

        // when
        ReviewDto result = reviewService.updateReview(reviewId, userId, updateRequest);

        // then
        assertThat(realReview.getContent()).isEqualTo("완전 명작이네요!");
        assertThat(realReview.getRating()).isEqualTo(5);

        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("완전 명작이네요!");
        assertThat(result.rating()).isEqualTo(5);
    }

    @Test
    @DisplayName("리뷰 수정 실패: 존재하지 않는 리뷰 ID로 수정을 요청하면 ReviewException이 발생한다.")
    void updateReview_Fail_NotFound() {
        // given
        UUID nonExistentReviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ReviewUpdateRequest updateRequest = new ReviewUpdateRequest("내용 수정", 5);

        given(reviewRepository.findByFetchJoin(nonExistentReviewId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewService.updateReview(nonExistentReviewId, userId, updateRequest))
            .isInstanceOf(ReviewException.class);

        verify(reviewRepository).findByFetchJoin(nonExistentReviewId);
        verifyNoInteractions(reviewLikeRepository);
    }

    @Test
    @DisplayName("리뷰 수정 실패: 작성자가 아닌 다른 유저가 수정을 요청하면 ReviewException이 발생한다.")
    void updateReview_Fail_ForbiddenUser() {
        // given
        User mockUser1 = createMockUser("test@test.com", "수달짱", "password1234!");
        Book mockBook = createMockBook(mockUser1, "도서명", "작가");

        Review mockReview = createMockReview(mockUser1, mockBook);
        UUID reviewId = mockReview.getId();

        User mockUser2 = createMockUser("stranger@test.com", "해달짱", "qwer1234@@");
        UUID strangerUserId = mockUser2.getId();

        ReviewUpdateRequest updateRequest = new ReviewUpdateRequest("남의 리뷰 몰래 수정!", 5);

        given(reviewRepository.findByFetchJoin(reviewId)).willReturn(Optional.of(mockReview));

        // when
        assertThatThrownBy(() -> reviewService.updateReview(reviewId, strangerUserId, updateRequest))
            .isInstanceOf(ReviewException.class);

        // then
        verify(reviewRepository).findByFetchJoin(reviewId);

        assertThat(mockReview.getContent()).isEqualTo("재미없어요");
        assertThat(mockReview.getRating()).isEqualTo(1);
    }

    // < DELETE >
    @Test
    @DisplayName("리뷰 논리삭제 성공: SoftDelete 로직이 정상 작동하고 종료된다.")
    void softDeleteReview_Success() {
        // given
        User mockUser = createMockUser("test@test.com", "수달짱", "password1234!");
        Book mockBook = createMockBook(mockUser, "도서명", "작가");

        Review mockReview = createMockReview(mockUser, mockBook);

        UUID requestUserId = mockUser.getId();
        UUID reviewId = mockReview.getId();

        given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(mockReview));
        given(bookRepository.removeReview(any(), anyInt())).willReturn(1);

        // when
        reviewService.reviewSoftDelete(reviewId, requestUserId);

        // then
        verify(reviewRepository).findByIdAndIsDeletedFalse(reviewId);
        assertThat(mockReview.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("리뷰 물리삭제 성공: 권한이 있는 유저가 삭제를 요청하면 데이터가 완전히 삭제된다.")
    void hardDeleteReview_Success() {
        // given
        User mockUser = createMockUser("test@test.com", "수달짱", "password1234!");
        Book mockBook = createMockBook(mockUser, "도서명", "작가");
        Review mockReview = createMockReview(mockUser, mockBook, "삭제될 리뷰", 5);

        UUID requestUserId = mockUser.getId();
        UUID reviewId = mockReview.getId();

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(mockReview));

        // when
        reviewService.reviewHardDelete(reviewId, requestUserId);

        // then
        verify(reviewRepository).delete(mockReview);
    }

    @Test
    @DisplayName("리뷰 논리삭제 실패: 존재하지 않거나 이미 삭제된 리뷰 ID로 삭제를 요청하면 ReviewException이 발생한다.")
    void softDeleteReview_Fail_NotFound() {
        // given
        UUID noReviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(reviewRepository.findByIdAndIsDeletedFalse(noReviewId)).willReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> reviewService.reviewSoftDelete(noReviewId, userId))
            .isInstanceOf(ReviewException.class);

        // then
        verify(reviewRepository).findByIdAndIsDeletedFalse(noReviewId);
        verifyNoInteractions(reviewMapper);
    }

    @Test
    @DisplayName("리뷰 논리삭제 실패: 작성자가 아닌 다른 유저가 삭제를 요청하면 ReviewException이 발생한다.")
    void softDeleteReview_Fail_ForbiddenUser() {
        // given
        User mockUser1 = createMockUser("test@test.com", "수달짱", "password1234!");
        Book mockBook = createMockBook(mockUser1, "도서명", "작가");
        Review mockReview = createMockReview(mockUser1, mockBook);
        User mockUser2 = createMockUser("asdf@test.com", "해달짱", "sdv1234@@@");
        UUID mockUser2Id = mockUser2.getId();
        // when & then
        assertThatThrownBy(() -> mockReview.softDelete(mockUser2Id))
            .isInstanceOf(ReviewException.class);

        assertThat(mockReview.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("리뷰 물리삭제 실패: 존재하지 않거나 이미 삭제된 리뷰 ID로 삭제를 요청하면 ReviewException이 발생한다.")
    void hardDeleteReview_Fail_NotFound() {
        // given
        User mockUser = createMockUser("test@test.com", "수달짱", "password1234!");
        Book mockBook = createMockBook(mockUser, "도서명", "작가");

        UUID mockUserId = mockUser.getId();
        UUID mockReviewId = UUID.randomUUID();

        given(reviewRepository.findById(mockReviewId)).willReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> reviewService.reviewHardDelete(mockReviewId, mockUserId))
            .isInstanceOf(ReviewException.class);

        // then
        verify(reviewRepository).findById(mockReviewId);
    }

}
