package com.sprint.deokhugam.review.application.port.in;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewLikeDto;
import com.sprint.deokhugam.review.application.port.out.ReviewLikeRepositoryPort;
import com.sprint.deokhugam.review.application.port.out.ReviewRepositoryPort;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.review.domain.ReviewLike;
import com.sprint.deokhugam.review.domain.exception.ReviewErrorCode;
import com.sprint.deokhugam.review.domain.exception.ReviewException;
import com.sprint.deokhugam.user.application.port.out.UserRepositoryPort;
import com.sprint.deokhugam.user.domain.User;
import com.sprint.deokhugam.user.domain.exception.UserErrorCode;
import com.sprint.deokhugam.user.domain.exception.UserException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewLikeService implements ReviewLikeUseCase {

    private final ReviewRepositoryPort reviewRepositoryPort;
    private final ReviewLikeRepositoryPort reviewLikeRepositoryPort;

    //private final NotificationUseCase notificationUseCase; // 알림 서비스 (생성 부분 파라미터 어떻게 만들었는지 물어봐야 함)
    private final UserRepositoryPort userRepositoryPort; // 유저 서비스 (상세 조회 부분 파라미터 어떻게 만들었는지 물어봐야 함)
    // + 나중에 유저 서비스로 바꿔야 함

    // '좋아요' 토글
    @Transactional
    public ReviewLikeDto toggleReviewLike(UUID reviewId, UUID requestId) {
        // '좋아요' 요청한 유저, 리뷰를 조회
        // ******** 유저 상세 조회 서비스 호출하는 코드로 바꿔야 함 (성준님 파트 - 물어볼거) ******************
        User requestUser = userRepositoryPort.findById(requestId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        // *****************************************************************

        Review review = reviewRepositoryPort.findById(reviewId)
            .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));

        // '리뷰 좋아요' 테이블에서 옵셔널 형태로 데이터를 가져옴
        Optional<ReviewLike> alreadyLike = reviewLikeRepositoryPort.findByReviewAndUser(review, requestUser);

        //  기록 O -> '좋아요' 취소
        //  기록 x -> 원래 만들었던 '좋아요' 추가
        if (alreadyLike.isPresent()) {
            review.decreaseLikeCount();
            reviewRepositoryPort.save(review);
            reviewLikeRepositoryPort.delete(alreadyLike.get());
            return new ReviewLikeDto(review.getId(), requestUser.getId(), false);
        } else {
            review.increaseLikeCount();
            reviewRepositoryPort.save(review);
            // ** + 알림서비스 호출해야됨 + **
            ReviewLike reviewLike = ReviewLike.create(review, requestUser);
            reviewLikeRepositoryPort.save(reviewLike);
            return new ReviewLikeDto(review.getId(), requestUser.getId(), true);
        }
    }
}



