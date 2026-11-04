package com.sprint.deokhugam.dashboard.application.port.out.popularreview;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PopularReview;

public interface PopularReviewRepositoryPort extends JpaRepository<PopularReview, UUID>, PopularReviewRepositoryCustom {

    long countByPeriodAndSnapshotDate(PeriodType period, LocalDate snapshotDate);

    @Modifying
    @Query("delete from PopularReview pr where pr.period = :period and pr.snapshotDate = :snapshotDate")
    void deleteByPeriodAndSnapshotDate(PeriodType period, LocalDate snapshotDate);
}
