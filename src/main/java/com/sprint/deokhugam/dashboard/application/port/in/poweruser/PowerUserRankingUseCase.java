package com.sprint.deokhugam.dashboard.application.port.in.poweruser;

import java.time.LocalDate;

import com.sprint.deokhugam.dashboard.domain.PeriodType;

public interface PowerUserRankingUseCase {

    void rankUsers(LocalDate baseDate);

}
