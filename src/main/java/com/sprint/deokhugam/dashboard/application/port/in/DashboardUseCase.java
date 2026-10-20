package com.sprint.deokhugam.dashboard.application.port.in;

import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularBookDto;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularBookRequest;

public interface DashboardUseCase {

    CursorPageResponse<PopularBookDto> getPopularBooks(PopularBookRequest request);
}
