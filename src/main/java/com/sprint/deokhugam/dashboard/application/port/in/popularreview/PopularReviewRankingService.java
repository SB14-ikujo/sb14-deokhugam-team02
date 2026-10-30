package com.sprint.deokhugam.dashboard.application.port.in.popularreview;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.dashboard.application.port.out.popularreview.PopularReviewRepositoryPort;
import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PopularReview;
import com.sprint.deokhugam.dashboard.domain.PopularReviewRanker;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopularReviewRankingService implements PopularReviewRankingUseCase {

    private final PopularReviewRepositoryPort popularReviewRepositoryPort;
    private final PopularReviewRanker popularReviewRanker = new PopularReviewRanker();

    @Override
    @Transactional
    public int rankReviews(LocalDate baseDate) {
        log.debug("인기 리뷰 집계 시작: baseDate={}", baseDate);
        int total = 0;

        for (PeriodType period : PeriodType.values()) {
            popularReviewRepositoryPort.deleteByPeriodAndSnapshotDate(period, baseDate);

            List<PopularReview> candidates = popularReviewRepositoryPort.findCandidates(period, baseDate);

            List<PopularReview> ranked = popularReviewRanker.rank(candidates);

            popularReviewRepositoryPort.saveAll(ranked);
            total += ranked.size();

            log.info("인기 리뷰 집계 완료: baseDate={}, period={}, count={}", baseDate, period, ranked.size());

        }

        return total;
    }
}
