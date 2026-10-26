package com.sprint.deokhugam.dashboard.application.port.in.popularreview;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sprint.deokhugam.dashboard.application.port.out.popularreview.PopularReviewRepositoryPort;
import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PopularReview;
import com.sprint.deokhugam.review.domain.Review;

@ExtendWith(MockitoExtension.class)
class PopularReviewRankingServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);

    @Mock
    private PopularReviewRepositoryPort popularReviewRepositoryPort;

    @InjectMocks
    private PopularReviewRankingService popularReviewRankingService;

    @Test
    @DisplayName("기간마다 기존 순위를 지우고, 후보를 조회해 저장하는 순서로 집계한다")
    void rankReviewsForEveryPeriodInOrder() {
        popularReviewRankingService.rankReviews(TODAY);

        InOrder inOrder = inOrder(popularReviewRepositoryPort);
        for (PeriodType period : PeriodType.values()) {
            inOrder.verify(popularReviewRepositoryPort).deleteByPeriodAndSnapshotDate(period, TODAY);
            inOrder.verify(popularReviewRepositoryPort).findCandidates(period, TODAY);
            inOrder.verify(popularReviewRepositoryPort).saveAll(List.of());
        }
    }

    @Test
    @DisplayName("후보에 순위를 매겨 점수 높은 순으로 저장한다")
    void rankReviewsSavesRankedCandidates() {
        PopularReview lowScore = candidate("A", 1, 0.7);
        PopularReview highScore = candidate("B", 2, 2.3);
        when(popularReviewRepositoryPort.findCandidates(PeriodType.DAILY, TODAY))
            .thenReturn(List.of(lowScore, highScore));

        popularReviewRankingService.rankReviews(TODAY);

        assertThat(highScore.getRank()).isEqualTo(1L);
        assertThat(lowScore.getRank()).isEqualTo(2L);
        verify(popularReviewRepositoryPort).saveAll(List.of(highScore, lowScore));
    }

    private PopularReview candidate(String content, long idNumber, double score) {
        Review review = Review.create(null, null, content, 5);
        ReflectionTestUtils.setField(review, "id", new UUID(0L, idNumber));
        return PopularReview.create(review, PeriodType.DAILY, null, score, 1L, 1L, TODAY);
    }
}
