package com.sprint.deokhugam.review.application.port.in;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.common.context.user.UserContextHolder;
import com.sprint.deokhugam.notification.application.port.in.NotificationUseCase;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewLikeDto;
import com.sprint.deokhugam.review.adapter.in.web.mapper.ReviewLikeMapper;
import com.sprint.deokhugam.review.application.port.out.ReviewCommandRepositoryPort;
import com.sprint.deokhugam.review.application.port.out.ReviewLikeRepositoryPort;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.review.domain.ReviewLike;
import com.sprint.deokhugam.review.domain.exception.ReviewErrorCode;
import com.sprint.deokhugam.review.domain.exception.ReviewException;
import com.sprint.deokhugam.review.domain.exception.ReviewLikeErrorCode;
import com.sprint.deokhugam.review.domain.exception.ReviewLikeException;
import com.sprint.deokhugam.user.application.port.in.UserUseCase;
import com.sprint.deokhugam.user.domain.User;
import com.sprint.deokhugam.user.domain.exception.UserErrorCode;
import com.sprint.deokhugam.user.domain.exception.UserException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewLikeService implements ReviewLikeUseCase {

    private final ReviewLikeMapper reviewLikeMapper;
    private final NotificationUseCase notificationUseCase;
    private final UserUseCase userUseCase;

    private final ReviewCommandRepositoryPort reviewCommandRepositoryPort;
    private final ReviewLikeRepositoryPort reviewLikeRepositoryPort;

    // '좋아요' 토글
    @Transactional
    public ReviewLikeDto reviewLikeToggle(UUID reviewId) {
        // 스레드 로컬에 저장된 유저 요청 가져옴
        UUID requestUserId = UserContextHolder.getUserId();

        // 요청한 유저의 Id 가 null일때 예외 처리
        if (requestUserId == null) {
            throw new UserException(UserErrorCode.USER_NOT_FOUND); // 프로젝트에 맞는 에러코드를 사용하세요
        }

        // '좋아요' 요청한 유저, 리뷰를 조회
        User requestUser = userUseCase.findUserById(requestUserId);

        Review review = reviewCommandRepositoryPort.findById(reviewId)
            .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));

        // '리뷰 좋아요' 테이블에서 옵셔널 형태로 데이터를 가져옴
        Optional<ReviewLike> alreadyLike = reviewLikeRepositoryPort.findByReviewAndUser(review, requestUser);

        //  기록 O -> '좋아요' 취소 (if)
        //  기록 x -> 원래 만들었던 '좋아요' 추가 (else)
        if (alreadyLike.isPresent()) {
            reviewCommandRepositoryPort.decreaseLikeCount(
                requestUserId); // 리파지토리로 DB에 있는 리뷰의 좋아요 수 감소 및 영속성 컨텍스트에 저장된 것 삭제
            review.downLikeCount();  // 리뷰 엔티티에서 좋아요 수 감소

            // 좋아요 데이터 삭제
            ReviewLike reviewLike = reviewLikeRepositoryPort.findByReviewAndUser(review, requestUser)
                .orElseThrow(() -> new ReviewLikeException(ReviewLikeErrorCode.REVIEW_LIKE_NOT_FOUNT));
            reviewLikeRepositoryPort.delete(alreadyLike.get());

            // 삭제된 좋아요 데이터 반환
            return reviewLikeMapper.toDto(reviewLike, false);
        } else {
            reviewCommandRepositoryPort.increaseLikeCount(
                requestUserId); // 리파지토리로 DB에 있는 리뷰의 좋아요 수 증가 및 영속성 컨텍스트에 저장된 것 삭제
            review.upLikeCount(); // 리뷰 엔티티에서 좋아요 수 증가

            notificationUseCase.createLikeNotification(review, requestUser.getNickname()); // 알람 생성

            ReviewLike reviewLike = ReviewLike.create(review, requestUser);
            reviewLikeRepositoryPort.save(reviewLike);

            // 생성된 좋아요 데이터 반환
            return reviewLikeMapper.toDto(reviewLike, true);
        }
    }
}




