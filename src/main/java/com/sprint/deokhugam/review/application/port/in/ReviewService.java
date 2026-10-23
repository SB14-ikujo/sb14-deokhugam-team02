package com.sprint.deokhugam.review.application.port.in;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.book.application.port.in.BookUseCase;
import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewDto;
import com.sprint.deokhugam.review.adapter.in.web.mapper.ReviewMapper;
import com.sprint.deokhugam.review.application.port.out.ReviewRepositoryPort;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.review.domain.exception.ReviewErrorCode;
import com.sprint.deokhugam.review.domain.exception.ReviewException;
import com.sprint.deokhugam.user.application.port.in.UserUseCase;
import com.sprint.deokhugam.user.domain.User;

@Service
@RequiredArgsConstructor
public class ReviewService implements ReviewUseCase {
    
    private final ReviewRepositoryPort reviewRepositoryPort;
    private final UserUseCase userUseCase;
    private final BookUseCase bookUseCase;
    private final ReviewMapper reviewMapper;

    // 리뷰 생성 (도서 별 1개의 리뷰만 등록할 수 있음)
    public ReviewDto createReview(UUID bookId, UUID userId, String content, Integer rating) {
        // 리뷰 생성 요청한 사용자 조회
        User requestUser = userUseCase.findById(userId);
        // 도서 조회
        Book targetBook = bookUseCase.findById(bookId);

        Optional<Review> wrappedReview = reviewRepositoryPort.findByUserAndBook(requestUser, targetBook);

        // 리뷰 O => 리뷰 생성 불가
        // 리뷰 x => 리뷰 생성
        if (wrappedReview.isPresent()) {
            throw new ReviewException(ReviewErrorCode.REVIEW_DUPLICATE);
        }
        // 리뷰 생성
        Review creatReview = Review.create(requestUser, targetBook, content, rating);
        Review createdReview = reviewRepositoryPort.save(creatReview);
        return reviewMapper.toDto(createdReview);
    }
}
