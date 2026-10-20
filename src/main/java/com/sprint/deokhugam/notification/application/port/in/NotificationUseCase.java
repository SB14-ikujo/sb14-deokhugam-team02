package com.sprint.deokhugam.notification.application.port.in;

import com.sprint.deokhugam.review.domain.Review;

public interface NotificationUseCase {

    void createLikeNotification(Review review, String actorNickname);

    void createCommentNotification(Review review, String actorNickname);
}
