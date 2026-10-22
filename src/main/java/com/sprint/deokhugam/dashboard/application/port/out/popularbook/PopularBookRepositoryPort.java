package com.sprint.deokhugam.dashboard.application.port.out.popularbook;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PopularBook;

public interface PopularBookRepositoryPort extends JpaRepository<PopularBook, UUID>, PopularBookRepositoryCustom {

    long countByPeriodAndSnapshotDate(PeriodType period, LocalDate snapshotDate);

    @Modifying
    @Query("delete from PopularBook pb where pb.period = :period and pb.snapshotDate = :snapshotDate")
    void deleteByPeriodAndSnapshotDate(PeriodType period, LocalDate snapshotDate);

}
