package com.sprint.deokhugam.dashboard.adapter.in.web;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularBookDto;
import com.sprint.deokhugam.dashboard.application.port.in.DashboardUseCase;
import com.sprint.deokhugam.dashboard.domain.PeriodType;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardUseCase dashboardUseCase;

    @Test
    @DisplayName("인기 도서 목록을 요청하면 커서 페이지 형식으로 응답한다.")
    void getPopularBooks() throws Exception {
        PopularBookDto book = new PopularBookDto(UUID.randomUUID(), UUID.randomUUID(), "인기 도서 1", "저자 1",
            null, PeriodType.DAILY, 1L, 99.0, 10L, 4.5, Instant.parse("2026-10-19T00:00:00Z"));
        when(dashboardUseCase.getPopularBooks(any()))
            .thenReturn(new CursorPageResponse<>(List.of(book), "1", book.createdAt(), 1,
                60L, true));

        mockMvc.perform(get("/api/books/popular"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].title").value("인기 도서 1"))
            .andExpect(jsonPath("$.content[0].rank").value(1))
            .andExpect(jsonPath("$.content[0].period").value("DAILY"))
            .andExpect(jsonPath("$.nextCursor").value("1"))
            .andExpect(jsonPath("$.hasNext").value(true))
            .andExpect(jsonPath("$.totalElements").value(60));
    }

}
