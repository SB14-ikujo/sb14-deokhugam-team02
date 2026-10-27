package com.sprint.deokhugam.dashboard.application.port.out.poweruser;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PowerUser;

public interface PowerUserRepositoryPort extends JpaRepository<PowerUser, UUID>, PowerUserRepositoryCustom {

    long countByPeriodAndSnapshotDate(PeriodType period, LocalDate snapshotDate);
}
