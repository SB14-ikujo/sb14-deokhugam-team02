package com.sprint.deokhugam.dashboard.application.port.out.poweruser;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.sprint.deokhugam.dashboard.domain.Direction;
import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PowerUser;

public interface PowerUserRepositoryCustom {

    Optional<LocalDate> findLatestSnapshotDate(PeriodType period);

    List<PowerUser> findPage(PeriodType period, LocalDate snapshotDate, Direction direction,
        Long cursorRank, int size);

    List<PowerUser> findCandidates(PeriodType period, LocalDate snapshotDate);
}
