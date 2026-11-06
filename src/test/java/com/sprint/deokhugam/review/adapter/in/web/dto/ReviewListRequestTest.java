package com.sprint.deokhugam.review.adapter.in.web.dto;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

class ReviewListRequestTest {

    @Test
    @DisplayName("모든 값이 null로 들어왔을 때 내부 기본값(디폴트) 분기 로직을 완벽하게 밟는다.")
    void reviewListRequest_DefaultValue_Coverage_100() {
        // given
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        String keyword = "해리포터";
        String cursor = "4.0";
        Instant after = Instant.now();

        ReviewListRequest request = new ReviewListRequest(
            userId,
            bookId,
            keyword,
            null,
            null,
            cursor,
            after,
            null
        );

        // then
        assertThat(request.userId()).isEqualTo(userId);
        assertThat(request.bookId()).isEqualTo(bookId);
        assertThat(request.keyword()).isEqualTo(keyword);

        assertThat(request.orderBy()).isEqualTo("createdAt");
        assertThat(request.direction()).isEqualTo(Sort.Direction.DESC);
        assertThat(request.limit()).isEqualTo(50);

        assertThat(request.cursor()).isEqualTo(cursor);
        assertThat(request.after()).isEqualTo(after);
    }
}
