package com.sprint.deokhugam.dashboard.adapter.in.web;

import org.springdoc.core.annotations.ParameterObject;
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
        @Valid @ParameterObject PopularBookRequest request
    ) {

        return dashboardUseCase.getPopularBooks(request);
    }
}
