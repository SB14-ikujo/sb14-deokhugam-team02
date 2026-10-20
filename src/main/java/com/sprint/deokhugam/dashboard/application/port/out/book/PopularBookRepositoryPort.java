package com.sprint.deokhugam.dashboard.application.port.out.book;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PopularBook;

public interface PopularBookRepositoryPort extends JpaRepository<PopularBook, UUID>, PopularBookRepositoryCustom {

    long countByPeriodAndSnapshotDate(PeriodType period, LocalDate snapshotDate);

}
