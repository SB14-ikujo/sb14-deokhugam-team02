package com.sprint.deokhugam.review.application.port.in;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.notification.application.port.in.NotificationUseCase;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewLikeDto;
import com.sprint.deokhugam.review.adapter.in.web.mapper.ReviewLikeMapper;
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

    private final ReviewLikeMapper reviewLikeMapper;
    private final NotificationUseCase notificationUseCase;
    private final UserRepositoryPort userRepositoryPort;
    private final ReviewRepositoryPort reviewRepositoryPort;
    private final ReviewLikeRepositoryPort reviewLikeRepositoryPort;

    // '좋아요' 토글
    @Transactional
    public ReviewLikeDto reviewLikeToggle(UUID reviewId, UUID requestUserId) {

        Optional<ReviewLike> alreadyLike = reviewLikeRepositoryPort.findByReviewIdAndUserId(reviewId, requestUserId);

        if (alreadyLike.isPresent()) {
            ReviewLike targetLike = alreadyLike.get();

            reviewRepositoryPort.decreaseLikeCount(reviewId);
            reviewLikeRepositoryPort.delete(targetLike);

            return reviewLikeMapper.toDto(targetLike, false);

        } else {
            reviewRepositoryPort.increaseLikeCount(reviewId);

            Review review = reviewRepositoryPort.findById(reviewId)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));

            User requestUser = userRepositoryPort.findById(requestUserId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

            notificationUseCase.createLikeNotification(review, requestUser.getNickname()); // 알림 생성

            ReviewLike newLike = ReviewLike.create(review, requestUser);
            reviewLikeRepositoryPort.save(newLike);

            return reviewLikeMapper.toDto(newLike, true);
        }
    }
}




