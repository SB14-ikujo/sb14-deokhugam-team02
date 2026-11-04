package com.sprint.deokhugam.dashboard.application.port.out.poweruser;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PowerUser;

public interface PowerUserRepositoryPort extends JpaRepository<PowerUser, UUID>, PowerUserRepositoryCustom {

    long countByPeriodAndSnapshotDate(PeriodType period, LocalDate snapshotDate);

    @Modifying
    @Query("delete from PowerUser pu where pu.period = :period and pu.snapshotDate = :snapshotDate")
    void deleteByPeriodAndSnapshotDate(PeriodType period, LocalDate snapshotDate);
}
