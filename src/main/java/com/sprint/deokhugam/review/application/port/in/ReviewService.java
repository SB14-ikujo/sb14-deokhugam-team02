package com.sprint.deokhugam.review.application.port.in;

import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.book.application.port.out.BookRepositoryPort;
import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.book.domain.exception.BookErrorCode;
import com.sprint.deokhugam.book.domain.exception.BookException;
import com.sprint.deokhugam.common.context.user.UserContextHolder;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewCreateRequest;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewDto;
import com.sprint.deokhugam.review.adapter.in.web.mapper.ReviewMapper;
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
    private final UserRepositoryPort userRepositoryPort;
    private final BookRepositoryPort bookRepositoryPort;
    private final ReviewMapper reviewMapper;

    // 리뷰 생성 (도서 별 1개의 리뷰만 등록할 수 있음)
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
    @Transactional
    public void reviewSoftDelete(UUID reviewId) {
        // 요청자 Id
        UUID requestUserId = UserContextHolder.getUserId();

        // 리뷰 가져오기
        Review review = reviewRepositoryPort.findByIdAndIsDeletedFalse(reviewId)
            .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));

        // 논리적 삭제(Soft-delete) 하기
        review.softDelete(requestUserId);
    }
}
