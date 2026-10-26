package com.sprint.deokhugam.dashboard.application.port.out.popularreview;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PopularReview;

public interface PopularReviewRepositoryPort extends JpaRepository<PopularReview, UUID>, PopularReviewRepositoryCustom {

    long countByPeriodAndSnapshotDate(PeriodType period, LocalDate snapshotDate);
}
