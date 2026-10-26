package com.sprint.deokhugam.dashboard.application.port.in;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularBookDto;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularBookRequest;
import com.sprint.deokhugam.dashboard.adapter.in.web.mapper.DashboardMapper;
import com.sprint.deokhugam.dashboard.application.port.out.popularbook.PopularBookRepositoryPort;
import com.sprint.deokhugam.dashboard.domain.Direction;
import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PopularBook;
import com.sprint.deokhugam.dashboard.domain.exception.DashboardErrorCode;
import com.sprint.deokhugam.dashboard.domain.exception.DashboardException;

@ExtendWith(MockitoExtension.class)
class DashboardApplicationServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);

    @Mock
    private DashboardMapper dashboardMapper;

    @Mock
    private PopularBookRepositoryPort popularBookRepositoryPort;

    @InjectMocks
    private DashboardApplicationService dashboardApplicationService;

    @Test
    @DisplayName("스냅샷이 없으면 빈 목록을 반환하고 페이지를 조회하지 않는다.")
    void returnsEmptyWhenNoSnapshot() {
        when(popularBookRepositoryPort.findLatestSnapshotDate(PeriodType.DAILY)).thenReturn(Optional.empty());

        CursorPageResponse<PopularBookDto> result = dashboardApplicationService.getPopularBooks(request(null, 2));

        assertThat(result.content()).isEmpty();
        assertThat(result.hasNext()).isFalse();
        verify(popularBookRepositoryPort, never()).findPage(any(), any(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("limit 이하로 조회되면 마지막 페이지다")
    void lastPageWhenRowsWithinLimit() {
        givenSnapshot();
        PopularBook first = mock(PopularBook.class);
        when(popularBookRepositoryPort.findPage(PeriodType.DAILY, TODAY, Direction.ASC, null, 3))
            .thenReturn(List.of(first));
        when(popularBookRepositoryPort.countByPeriodAndSnapshotDate(PeriodType.DAILY, TODAY)).thenReturn(1L);
        when(dashboardMapper.toDto(first)).thenReturn(dto(1L));

        CursorPageResponse<PopularBookDto> result = dashboardApplicationService.getPopularBooks(request(null, 2));

        assertThat(result.content()).hasSize(1);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
        assertThat(result.totalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("limit보다 하나 더 조회되면 다음 페이지가 있고 마지막 순위가 커서가 된다")
    void hasNextWhenRowsExceedLimit() {
        givenSnapshot();
        Instant lastCreatedAt = Instant.parse("2026-10-19T00:00:00Z");
        PopularBook first = mock(PopularBook.class);
        PopularBook second = mock(PopularBook.class);
        PopularBook extra = mock(PopularBook.class);
        when(popularBookRepositoryPort.findPage(PeriodType.DAILY, TODAY, Direction.ASC, null, 3))
            .thenReturn(List.of(first, second, extra));
        when(popularBookRepositoryPort.countByPeriodAndSnapshotDate(PeriodType.DAILY, TODAY)).thenReturn(10L);
        when(dashboardMapper.toDto(first)).thenReturn(dto(1L));
        when(dashboardMapper.toDto(second)).thenReturn(dto(2L, lastCreatedAt));

        CursorPageResponse<PopularBookDto> result = dashboardApplicationService.getPopularBooks(request(null, 2));

        assertThat(result.content()).hasSize(2);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo("2");
        assertThat(result.nextAfter()).isEqualTo(lastCreatedAt);
        assertThat(result.totalElements()).isEqualTo(10L);
    }

    @Test
    @DisplayName("커서를 순위 숫자로 바꿔 조회한다")
    void passesCursorAsRank() {
        givenSnapshot();

        dashboardApplicationService.getPopularBooks(request("50", 2));

        verify(popularBookRepositoryPort).findPage(PeriodType.DAILY, TODAY, Direction.ASC, 50L, 3);
    }

    @Test
    @DisplayName("커서가 숫자가 아니면 예외가 발생한다")
    void throwsWhenCursorIsNotNumber() {
        givenSnapshot();

        assertThatThrownBy(() -> dashboardApplicationService.getPopularBooks(request("abc", 2)))
            .isInstanceOf(DashboardException.class)
            .extracting("errorCode")
            .isEqualTo(DashboardErrorCode.DASHBOARD_INVALID_CURSOR);
    }

    private void givenSnapshot() {
        when(popularBookRepositoryPort.findLatestSnapshotDate(PeriodType.DAILY)).thenReturn(Optional.of(TODAY));
    }

    private PopularBookRequest request(String cursor, int limit) {
        return new PopularBookRequest(PeriodType.DAILY, Direction.ASC, cursor, null, limit);
    }

    private PopularBookDto dto(long rank) {
        return new PopularBookDto(UUID.randomUUID(), UUID.randomUUID(), "제목", "저자", null,
            PeriodType.DAILY, rank, 1.0, 1L, 4.0, Instant.now());
    }

    private PopularBookDto dto(long rank, Instant createdAt) {
        return new PopularBookDto(UUID.randomUUID(), UUID.randomUUID(), "제목", "저자", null,
            PeriodType.DAILY, rank, 1.0, 1L, 4.0, createdAt);
    }

}
