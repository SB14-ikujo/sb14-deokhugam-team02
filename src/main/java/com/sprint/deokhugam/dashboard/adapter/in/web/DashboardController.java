package com.sprint.deokhugam.dashboard.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularBookDto;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularBookRequest;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularReviewDto;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularReviewRequest;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PowerUserDto;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PowerUserRequest;
import com.sprint.deokhugam.dashboard.application.port.in.DashboardUseCase;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController implements DashboardApi {

    private final DashboardUseCase dashboardUseCase;

    @Override
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/books/popular")
    public CursorPageResponse<PopularBookDto> getPopularBooks(
        @Valid PopularBookRequest request
    ) {

        return dashboardUseCase.getPopularBooks(request);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/reviews/popular")
    public CursorPageResponse<PopularReviewDto> getPopularReviews(
        @Valid PopularReviewRequest request
    ) {

        return dashboardUseCase.getPopularReviews(request);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/users/power")
    public CursorPageResponse<PowerUserDto> getPowerUsers(
        @Valid PowerUserRequest request
    ) {

        return dashboardUseCase.getPowerUsers(request);
    }
}
