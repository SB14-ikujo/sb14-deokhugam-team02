package com.sprint.deokhugam.dashboard.application.port.in.popularbook;

import java.time.LocalDate;

public interface PopularBookRankingUseCase {

    void rankBooks(LocalDate baseDate);
}
