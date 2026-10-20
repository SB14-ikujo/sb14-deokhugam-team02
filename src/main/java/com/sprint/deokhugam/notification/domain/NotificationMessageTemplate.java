package com.sprint.deokhugam.notification.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public enum NotificationMessageTemplate {
    COMMENT("님이 나의 리뷰에 댓글을 남겼습니다."),
    LIKE("님이 나의 리뷰를 좋아합니다.");

    String messageTemplate;

    public String createMessageTemplate(String actorNickname) {
        return String.format("[%s]%s", actorNickname, this.messageTemplate);
    }
}
