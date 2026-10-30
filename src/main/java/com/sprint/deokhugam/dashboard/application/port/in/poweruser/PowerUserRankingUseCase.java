package com.sprint.deokhugam.dashboard.application.port.in.poweruser;

import java.time.LocalDate;

public interface PowerUserRankingUseCase {

    int rankUsers(LocalDate baseDate);

}
