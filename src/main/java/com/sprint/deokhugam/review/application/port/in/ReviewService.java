package com.sprint.deokhugam.review.application.port.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.book.application.port.out.BookRepositoryPort;
import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.book.domain.exception.BookErrorCode;
import com.sprint.deokhugam.book.domain.exception.BookException;
import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.common.util.SliceUtils;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewCreateRequest;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewDto;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewListRequest;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewUpdateRequest;
import com.sprint.deokhugam.review.adapter.in.web.mapper.ReviewMapper;
import com.sprint.deokhugam.review.application.port.out.ReviewLikeRepositoryPort;
import com.sprint.deokhugam.review.application.port.out.ReviewRepositoryPort;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.review.domain.exception.ReviewErrorCode;
import com.sprint.deokhugam.review.domain.exception.ReviewException;
import com.sprint.deokhugam.user.application.port.out.UserRepositoryPort;
import com.sprint.deokhugam.user.domain.User;
import com.sprint.deokhugam.user.domain.exception.UserErrorCode;
import com.sprint.deokhugam.user.domain.exception.UserException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService implements ReviewUseCase {

    private final ReviewRepositoryPort reviewRepositoryPort;
    private final ReviewLikeRepositoryPort reviewLikeRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final BookRepositoryPort bookRepositoryPort;
    private final ReviewMapper reviewMapper;

    // 리뷰 생성 (도서 별 1개의 리뷰만 등록할 수 있음)
    @Override
    @Transactional
    public ReviewDto createReview(ReviewCreateRequest createRequest) {

        UUID requestUserId = createRequest.userId(); // 요청한 유저 ID
        UUID targetBookId = createRequest.bookId(); // 리뷰를 남길 도서 ID

        // 요청한 유저 존재 유무 확인 및 데이터 가져오기 (소프트 딜리트 된 것들은 제외)
        User requestUser = userRepositoryPort.findByIdAndIsDeletedFalse(requestUserId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 해당 도서 존재 유무 확인 및 데이터 가져오기 (소프트 딜리트 된 것들은 제외)
        Book targetBook = bookRepositoryPort.findByIdAndIsDeletedFalse(targetBookId)
            .orElseThrow(() -> new BookException(BookErrorCode.BOOK_NOT_FOUND));

        // 기존에 리뷰가 있는지 확인
        Optional<Review> wrappedReview = reviewRepositoryPort.findValidReview(requestUserId, targetBookId);

        // 리뷰 O => 리뷰 생성 불가
        if (wrappedReview.isPresent()) {
            throw new ReviewException(ReviewErrorCode.REVIEW_DUPLICATE);
        }

        targetBook.increaseReviewCount(); // 해당 도서의 리뷰수 증가

        String content = createRequest.content();
        Integer rating = createRequest.rating();

        Review createReview = Review.create(requestUser, targetBook, content, rating);
        try {
            Review createdReview = reviewRepositoryPort.save(createReview);
            return reviewMapper.toDto(createdReview, false);
        } catch (DataIntegrityViolationException e) {
            throw new ReviewException(ReviewErrorCode.REVIEW_DUPLICATE);
        }
    }

    // 리뷰 소프트 딜리트
    @Override
    @Transactional
    public void reviewSoftDelete(UUID reviewId, UUID requestUserId) {
        // 해당 리뷰 가져오기 (논리적으로 삭제되지 않은 해당 리뷰만 가져오기)
        Review review = reviewRepositoryPort.findByIdAndIsDeletedFalse(reviewId)
            .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));

        // 논리적 삭제(Soft-delete) 하기
        review.softDelete(requestUserId);
    }

    // 리뷰 물리 삭제
    @Override
    @Transactional
    public void reviewHardDelete(UUID reviewId, UUID requestUserId) {
        // 해당 리뷰 가져오기
        Review review = reviewRepositoryPort.findById(reviewId)
            .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));

        // 해당 리뷰를 작성한 유저와 삭제를 요청한 유저가 같은지 확인
        UUID reviewWriterId = review.getUser().getId();
        if (!reviewWriterId.equals(requestUserId)) {
            throw new ReviewException(ReviewErrorCode.REVIEW_FORBIDDEN);
        }

        // 리뷰 데이터 물리 삭제
        reviewRepositoryPort.delete(review);
    }

    // 리뷰 상세 조회
    @Override
    public ReviewDto getReviewDetail(UUID reviewId, UUID requestUserId) {
        // 존재하는 유저인지 확인
        Optional<User> wrappedUser = userRepositoryPort.findByIdAndIsDeletedFalse(requestUserId);
        if (wrappedUser.isEmpty()) {
            throw new UserException(UserErrorCode.USER_NOT_FOUND);
        }

        // 해당 리뷰 찾기 (유저와 도서를 패치조인해서 한번에 가져옴)
        Review review = reviewRepositoryPort.findByFetchJoin(reviewId)
            .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));

        // 내가 이 리뷰에 좋아요를 남겼는지 확인
        boolean likedByMe = reviewLikeRepositoryPort.existsByReviewIdAndUserId(reviewId, requestUserId);

        // 반환
        return reviewMapper.toDto(review, likedByMe);
    }

    // 리뷰 수정 (본인이 작성한 리뷰만 수정할 수 있습니다.)
    @Override
    @Transactional
    public ReviewDto updateReview(UUID reviewId, UUID requestUserId, ReviewUpdateRequest updateRequest) {
        // 본인이 작성한 리뷰인지 확인
        Review review = reviewRepositoryPort.findByFetchJoin(reviewId)
            .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));

        UUID reviewWriterId = review.getUser().getId();

        if (!reviewWriterId.equals(requestUserId)) {
            throw new ReviewException(ReviewErrorCode.REVIEW_FORBIDDEN);
        }

        // 평점, 내용을 수정함
        review.update(updateRequest.content(), updateRequest.rating());

        // 내가 이 리뷰에 좋아요를 남겼는지 확인
        boolean likedByMe = reviewLikeRepositoryPort.existsByReviewIdAndUserId(reviewId, requestUserId);

        // 반환
        return reviewMapper.toDto(review, likedByMe);
    }

    // 리뷰 목록 조회
    @Override
    public CursorPageResponse<ReviewDto> getReviewList(ReviewListRequest request) {
        // 페이지 사이즈 (size)
        int limit = request.limit();

        // 커서 유효성을 미리 파싱하고 검증 (cursor)
        String validatedCursor = parseCursor(request.cursor(), request.orderBy());

        // 도서가 존재하는지 확인 (bookId)
        if (request.bookId() != null) {
            bookRepositoryPort.findByIdAndIsDeletedFalse(request.bookId())
                .orElseThrow(() -> new BookException(BookErrorCode.BOOK_NOT_FOUND));
        }

        // 유저가 존재하는지 확인 (userId)
        if (request.userId() != null) {
            userRepositoryPort.findByIdAndIsDeletedFalse(request.userId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        }

        // 리뷰 목록 데이터(엔티티들)
        List<Review> reviewEntities = reviewRepositoryPort.getReviewList(
            request.userId(),
            request.bookId(),
            request.keyword(),
            request.orderBy(),
            request.after(),
            validatedCursor,
            request.direction(),
            limit
        );

        // Dto로 변환
        List<ReviewDto> reviews = reviewEntities.stream()
            .map(review -> {
                boolean isLikedByMe = reviewLikeRepositoryPort.existsByReviewIdAndUserId(
                    review.getId(),
                    request.userId()
                );
                return reviewMapper.toDto(review, isLikedByMe);
            })
            .toList();

        Slice<ReviewDto> slice = SliceUtils.toSlice(reviews, limit);

        long total = reviewRepositoryPort.countByConditions(request.userId(), request.bookId(), request.keyword());

        return CursorPageResponse.of(
            slice,
            total,
            dto -> extractNextCursor(dto, request.orderBy()),
            ReviewDto::createdAt
        );
    }

    // 평점에 .0 붙여주는 메서드
    private String extractNextCursor(ReviewDto lastReviewDto, String orderBy) {
        if ("rating".equals(orderBy)) {
            return lastReviewDto.rating() + ".0"; // 평점순일 때는 .0을 붙여서 추출
        }
        return null; // 최신순일 때는 커서가 필요 없으므로 null 반환
    }

    private String parseCursor(String cursor, String orderBy) {
        // 평점순 정렬이 아니라 시간순 정렬이면 cursor 검증 필요 없음
        if (!"rating".equals(orderBy)) {
            return null;
        }
        // 첫 페이지 요청 (cursor가 비어있으면)
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            double rating = Double.parseDouble(cursor);

            if (rating < 1.0 || rating > 5.0) {
                throw new ReviewException(ReviewErrorCode.REVIEW_INVALID_CURSOR);
            }
        } catch (NumberFormatException e) {
            throw new ReviewException(ReviewErrorCode.REVIEW_INVALID_CURSOR);
        }

        // 검증이 완료된 cursor를 반환
        return cursor;
    }

}
