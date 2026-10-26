package com.sprint.deokhugam.notification.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sprint.deokhugam.dashboard.domain.PeriodType;

class NotificationMessageTemplateTest {

    @Test
    @DisplayName("알림 템플릿에 전달된 값을 포함하여 메시지를 생성한다")
    void createMessageTemplate_formatsMessageCorrectly() {
        NotificationMessageTemplate template = NotificationMessageTemplate.BEST_REVIEW;

        String dailyMessage = template.createMessageTemplate("테스트리뷰", PeriodType.DAILY);
        String weeklyMessage = template.createMessageTemplate("테스트리뷰", PeriodType.WEEKLY);
        String monthlyMessage = template.createMessageTemplate("테스트리뷰", PeriodType.MONTHLY);

        assertThat(dailyMessage).isEqualTo("[테스트리뷰] 리뷰가 일간 인기 리뷰 TOP 10에 선정되었습니다.");
        assertThat(weeklyMessage).isEqualTo("[테스트리뷰] 리뷰가 주간 인기 리뷰 TOP 10에 선정되었습니다.");
        assertThat(monthlyMessage).isEqualTo("[테스트리뷰] 리뷰가 월간 인기 리뷰 TOP 10에 선정되었습니다.");
    }

}
