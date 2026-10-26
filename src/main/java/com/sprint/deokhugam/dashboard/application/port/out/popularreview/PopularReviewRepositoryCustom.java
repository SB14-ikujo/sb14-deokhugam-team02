package com.sprint.deokhugam.dashboard.application.port.out.popularreview;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.sprint.deokhugam.dashboard.domain.Direction;
import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PopularReview;

public interface PopularReviewRepositoryCustom {

    Optional<LocalDate> findLatestSnapshotDate(PeriodType period);

    // 인기 리뷰 뽑아오기
    List<PopularReview> findPage(PeriodType period, LocalDate snapshotDate, Direction direction,
        Long cursorRank, int size);

    List<PopularReview> findCandidates(PeriodType period, LocalDate baseDate);

    // Top10 인기리뷰 가져오기
    List<PopularReview> findTop10ByPeriod(PeriodType period, LocalDate baseDate);
}
