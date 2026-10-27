package com.sprint.deokhugam.dashboard.adapter.in.web;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularBookDto;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PowerUserDto;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PowerUserRequest;
import com.sprint.deokhugam.dashboard.application.port.in.DashboardUseCase;
import com.sprint.deokhugam.dashboard.domain.Direction;
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

    @Test
    @DisplayName("파워 유저 목록을 요청하면 커서 페이지 형식으로 응답한다")
    void getPowerUsers() throws Exception {
        PowerUserDto powerUser = new PowerUserDto(UUID.randomUUID(), "파워유저", PeriodType.WEEKLY,
            Instant.parse("2026-10-19T00:00:00Z"), 1L, 9.5, 6.0, 3L, 2L);
        when(dashboardUseCase.getPowerUsers(any()))
            .thenReturn(new CursorPageResponse<>(List.of(powerUser), "1", powerUser.createdAt(), 1,
                30L, true));

        mockMvc.perform(get("/api/users/power").param("period", "WEEKLY"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].nickname").value("파워유저"))
            .andExpect(jsonPath("$.content[0].rank").value(1))
            .andExpect(jsonPath("$.content[0].period").value("WEEKLY"))
            .andExpect(jsonPath("$.content[0].reviewScoreSum").value(6.0))
            .andExpect(jsonPath("$.content[0].likeCount").value(3))
            .andExpect(jsonPath("$.content[0].commentCount").value(2))
            .andExpect(jsonPath("$.nextCursor").value("1"))
            .andExpect(jsonPath("$.hasNext").value(true))
            .andExpect(jsonPath("$.totalElements").value(30));
    }

    @Test
    @DisplayName("파워 유저 요청에 값이 없으면 DAILY, ASC, 50개를 기본값으로 쓴다")
    void getPowerUsersUsesDefaults() throws Exception {
        mockMvc.perform(get("/api/users/power"))
            .andExpect(status().isOk());

        ArgumentCaptor<PowerUserRequest> captor = ArgumentCaptor.forClass(PowerUserRequest.class);
        verify(dashboardUseCase).getPowerUsers(captor.capture());
        assertThat(captor.getValue().period()).isEqualTo(PeriodType.DAILY);
        assertThat(captor.getValue().direction()).isEqualTo(Direction.ASC);
        assertThat(captor.getValue().limit()).isEqualTo(50);
    }

    @ParameterizedTest(name = "limit={0}")
    @ValueSource(strings = {"0", "101"})
    @DisplayName("파워 유저 limit이 1~100 범위를 벗어나면 400을 반환하고 서비스를 호출하지 않는다")
    void getPowerUsersRejectsLimitOutOfRange(String limit) throws Exception {
        mockMvc.perform(get("/api/users/power").param("limit", limit))
            .andExpect(status().isBadRequest());

        verify(dashboardUseCase, never()).getPowerUsers(any());
    }

    @Test
    @DisplayName("파워 유저 기간 값이 잘못되면 400을 반환한다")
    void getPowerUsersRejectsInvalidPeriod() throws Exception {
        mockMvc.perform(get("/api/users/power").param("period", "YEARLY"))
            .andExpect(status().isBadRequest());

        verify(dashboardUseCase, never()).getPowerUsers(any());
    }

}
