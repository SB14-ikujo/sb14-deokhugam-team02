package com.sprint.deokhugam.dashboard.application.port.in.popularreview;

import java.time.LocalDate;

public interface PopularReviewRankingUseCase {

    int rankReviews(LocalDate baseDate);
}
