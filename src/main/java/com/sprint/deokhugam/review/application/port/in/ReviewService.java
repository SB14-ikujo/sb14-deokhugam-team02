package com.sprint.deokhugam.review.application.port.in;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.book.application.port.in.BookUseCase;
import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewCreateRequest;
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
@Transactional(readOnly = true)
public class ReviewService implements ReviewUseCase {

    private final ReviewRepositoryPort reviewRepositoryPort;
    private final UserUseCase userUseCase;
    private final BookUseCase bookUseCase;
    private final ReviewMapper reviewMapper;

    // 리뷰 생성 (도서 별 1개의 리뷰만 등록할 수 있음)
    @Transactional
    public ReviewDto createReview(ReviewCreateRequest createRequest) {

        UUID requestUserId = createRequest.userId(); // 요청한 유저 ID
        UUID targetBookId = createRequest.bookId(); // 리뷰를 남길 도서 ID

        // 기존에 리뷰가 있는지 확인
        Optional<Review> wrappedReview = reviewRepositoryPort.findByUserIdAndBookId(requestUserId, targetBookId);

        // 리뷰 O => 리뷰 생성 불가
        if (wrappedReview.isPresent()) {
            throw new ReviewException(ReviewErrorCode.REVIEW_DUPLICATE);
        }
        // 리뷰 x => 리뷰 생성
        User requestUser = userUseCase.findById(requestUserId);
        Book targetBook = bookUseCase.findById(targetBookId);
        String content = createRequest.content();
        Integer rating = createRequest.rating();
        Review creatReview = Review.create(requestUser, targetBook, content, rating);
        Review createdReview = reviewRepositoryPort.save(creatReview);
        return reviewMapper.toDto(createdReview, requestUser, targetBook, false); //  likedByMe: false
    }
}
