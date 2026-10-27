package com.sprint.deokhugam.dashboard.application.port.in;

import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularBookDto;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularBookRequest;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularReviewDto;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularReviewRequest;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PowerUserDto;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PowerUserRequest;

public interface DashboardUseCase {

    CursorPageResponse<PopularBookDto> getPopularBooks(PopularBookRequest request);

    CursorPageResponse<PopularReviewDto> getPopularReviews(PopularReviewRequest request);

    CursorPageResponse<PowerUserDto> getPowerUsers(PowerUserRequest request);
}
