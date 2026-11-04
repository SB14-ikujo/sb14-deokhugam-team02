package com.sprint.deokhugam.notification.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import com.sprint.deokhugam.dashboard.domain.PeriodType;

@Getter
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public enum NotificationMessageTemplate {
    BEST_REVIEW("[%s] 리뷰가 %s 인기 리뷰 TOP 10에 선정되었습니다."),
    COMMENT("[%s]님이 나의 리뷰에 댓글을 남겼습니다."),
    LIKE("[%s]님이 나의 리뷰를 좋아합니다.");

    String messageTemplate;

    public String createMessageTemplate(String actorNickname) {
        return String.format(messageTemplate, actorNickname);
    }

    public String createMessageTemplate(String reviewName, PeriodType periodType) {
        return String.format(messageTemplate, reviewName, periodType.getLabel());
    }
}
