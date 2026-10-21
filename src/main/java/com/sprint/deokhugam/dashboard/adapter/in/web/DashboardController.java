package com.sprint.deokhugam.dashboard.adapter.in.web;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
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
public class DashboardController {

    private final DashboardUseCase dashboardUseCase;

    @Tag(name = "도서 관리")
    @GetMapping("/books/popular")
    public ResponseEntity<CursorPageResponse<PopularBookDto>> getPopularBooks(
        @Valid @ParameterObject PopularBookRequest request
    ) {
        // 커서로 감싼 Dto로 반환받음
        CursorPageResponse<PopularBookDto> popularBooks = dashboardUseCase.getPopularBooks(request);

        return ResponseEntity.ok(popularBooks);
    }
}
