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

import com.sprint.deokhugam.book.application.port.out.BookImageStorage;
import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularBookDto;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularBookRequest;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PowerUserDto;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PowerUserRequest;
import com.sprint.deokhugam.dashboard.adapter.in.web.mapper.DashboardMapper;
import com.sprint.deokhugam.dashboard.application.port.out.popularbook.PopularBookRepositoryPort;
import com.sprint.deokhugam.dashboard.application.port.out.poweruser.PowerUserRepositoryPort;
import com.sprint.deokhugam.dashboard.domain.Direction;
import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PopularBook;
import com.sprint.deokhugam.dashboard.domain.PowerUser;
import com.sprint.deokhugam.dashboard.domain.exception.DashboardErrorCode;
import com.sprint.deokhugam.dashboard.domain.exception.DashboardException;

@ExtendWith(MockitoExtension.class)
class DashboardApplicationServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);

    @Mock
    private DashboardMapper dashboardMapper;

    @Mock
    private PopularBookRepositoryPort popularBookRepositoryPort;

    @Mock
    private PowerUserRepositoryPort powerUserRepositoryPort;

    @Mock
    private BookImageStorage bookImageStorage;

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
        PopularBook first = popularBookWithThumbnail(null);
        when(popularBookRepositoryPort.findPage(PeriodType.DAILY, TODAY, Direction.ASC, null, 3))
            .thenReturn(List.of(first));
        when(popularBookRepositoryPort.countByPeriodAndSnapshotDate(PeriodType.DAILY, TODAY)).thenReturn(1L);
        when(dashboardMapper.toDto(first, null)).thenReturn(dto(1L));

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
        PopularBook first = popularBookWithThumbnail(null);
        PopularBook second = popularBookWithThumbnail(null);
        PopularBook extra = mock(PopularBook.class); // limit 초과분은 DTO로 변환되지 않는다
        when(popularBookRepositoryPort.findPage(PeriodType.DAILY, TODAY, Direction.ASC, null, 3))
            .thenReturn(List.of(first, second, extra));
        when(popularBookRepositoryPort.countByPeriodAndSnapshotDate(PeriodType.DAILY, TODAY)).thenReturn(10L);
        when(dashboardMapper.toDto(first, null)).thenReturn(dto(1L));
        when(dashboardMapper.toDto(second, null)).thenReturn(dto(2L, lastCreatedAt));

        CursorPageResponse<PopularBookDto> result = dashboardApplicationService.getPopularBooks(request(null, 2));

        assertThat(result.content()).hasSize(2);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo("2");
        assertThat(result.nextAfter()).isEqualTo(lastCreatedAt);
        assertThat(result.totalElements()).isEqualTo(10L);
    }

    @Test
    @DisplayName("썸네일 키가 있으면 저장소에서 실제 URL로 바꿔 매퍼에 넘긴다")
    void resolvesThumbnailKeyToUrl() {
        givenSnapshot();
        PopularBook first = popularBookWithThumbnail("cover.png");
        when(popularBookRepositoryPort.findPage(PeriodType.DAILY, TODAY, Direction.ASC, null, 3))
            .thenReturn(List.of(first));
        when(popularBookRepositoryPort.countByPeriodAndSnapshotDate(PeriodType.DAILY, TODAY)).thenReturn(1L);
        when(bookImageStorage.getImageUrl("cover.png", "image/png")).thenReturn("/api/images/cover.png");
        when(dashboardMapper.toDto(first, "/api/images/cover.png")).thenReturn(dto(1L));

        CursorPageResponse<PopularBookDto> result = dashboardApplicationService.getPopularBooks(request(null, 2));

        assertThat(result.content()).hasSize(1);
        verify(bookImageStorage).getImageUrl("cover.png", "image/png");
        verify(dashboardMapper).toDto(first, "/api/images/cover.png");
    }

    @Test
    @DisplayName("썸네일 키가 비어 있으면 저장소를 호출하지 않고 URL 없이 넘긴다")
    void skipsStorageWhenThumbnailBlank() {
        givenSnapshot();
        PopularBook first = popularBookWithThumbnail("  ");
        when(popularBookRepositoryPort.findPage(PeriodType.DAILY, TODAY, Direction.ASC, null, 3))
            .thenReturn(List.of(first));
        when(popularBookRepositoryPort.countByPeriodAndSnapshotDate(PeriodType.DAILY, TODAY)).thenReturn(1L);
        when(dashboardMapper.toDto(first, null)).thenReturn(dto(1L));

        dashboardApplicationService.getPopularBooks(request(null, 2));

        verifyNoInteractions(bookImageStorage);
        verify(dashboardMapper).toDto(first, null);
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

    @Test
    @DisplayName("파워 유저 스냅샷이 없으면 빈 목록을 반환하고 페이지를 조회하지 않는다")
    void powerUsersReturnsEmptyWhenNoSnapshot() {
        when(powerUserRepositoryPort.findLatestSnapshotDate(PeriodType.DAILY)).thenReturn(Optional.empty());

        CursorPageResponse<PowerUserDto> result = dashboardApplicationService.getPowerUsers(powerUserRequest(null, 2));

        assertThat(result.content()).isEmpty();
        assertThat(result.hasNext()).isFalse();
        verify(powerUserRepositoryPort, never()).findPage(any(), any(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("파워 유저가 limit 이하로 조회되면 마지막 페이지다")
    void powerUsersLastPageWhenRowsWithinLimit() {
        givenPowerUserSnapshot();
        PowerUser first = mock(PowerUser.class);
        when(powerUserRepositoryPort.findPage(PeriodType.DAILY, TODAY, Direction.ASC, null, 3))
            .thenReturn(List.of(first));
        when(powerUserRepositoryPort.countByPeriodAndSnapshotDate(PeriodType.DAILY, TODAY)).thenReturn(1L);
        when(dashboardMapper.toDto(first)).thenReturn(powerUserDto(1L, Instant.now()));

        CursorPageResponse<PowerUserDto> result = dashboardApplicationService.getPowerUsers(powerUserRequest(null, 2));

        assertThat(result.content()).hasSize(1);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
        assertThat(result.totalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("파워 유저가 limit보다 하나 더 조회되면 다음 페이지가 있고 마지막 순위가 커서가 된다")
    void powerUsersHasNextWhenRowsExceedLimit() {
        givenPowerUserSnapshot();
        Instant lastCreatedAt = Instant.parse("2026-10-19T00:00:00Z");
        PowerUser first = mock(PowerUser.class);
        PowerUser second = mock(PowerUser.class);
        PowerUser extra = mock(PowerUser.class);
        when(powerUserRepositoryPort.findPage(PeriodType.DAILY, TODAY, Direction.ASC, null, 3))
            .thenReturn(List.of(first, second, extra));
        when(powerUserRepositoryPort.countByPeriodAndSnapshotDate(PeriodType.DAILY, TODAY)).thenReturn(10L);
        when(dashboardMapper.toDto(first)).thenReturn(powerUserDto(1L, Instant.now()));
        when(dashboardMapper.toDto(second)).thenReturn(powerUserDto(2L, lastCreatedAt));

        CursorPageResponse<PowerUserDto> result = dashboardApplicationService.getPowerUsers(powerUserRequest(null, 2));

        assertThat(result.content()).hasSize(2);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo("2");
        assertThat(result.nextAfter()).isEqualTo(lastCreatedAt);
        assertThat(result.totalElements()).isEqualTo(10L);
    }

    @Test
    @DisplayName("파워 유저 커서를 순위 숫자로 바꿔 조회한다")
    void powerUsersPassesCursorAsRank() {
        givenPowerUserSnapshot();

        dashboardApplicationService.getPowerUsers(powerUserRequest("50", 2));

        verify(powerUserRepositoryPort).findPage(PeriodType.DAILY, TODAY, Direction.ASC, 50L, 3);
    }

    @Test
    @DisplayName("파워 유저 커서가 숫자가 아니면 예외가 발생한다")
    void powerUsersThrowsWhenCursorIsNotNumber() {
        givenPowerUserSnapshot();

        assertThatThrownBy(() -> dashboardApplicationService.getPowerUsers(powerUserRequest("abc", 2)))
            .isInstanceOf(DashboardException.class)
            .extracting("errorCode")
            .isEqualTo(DashboardErrorCode.DASHBOARD_INVALID_CURSOR);
    }

    private void givenPowerUserSnapshot() {
        when(powerUserRepositoryPort.findLatestSnapshotDate(PeriodType.DAILY)).thenReturn(Optional.of(TODAY));
    }

    private PowerUserRequest powerUserRequest(String cursor, int limit) {
        return new PowerUserRequest(PeriodType.DAILY, Direction.ASC, cursor, null, limit);
    }

    private PowerUserDto powerUserDto(long rank, Instant createdAt) {
        return new PowerUserDto(UUID.randomUUID(), "닉네임", PeriodType.DAILY, createdAt, rank, 1.0, 1.0, 1L, 1L);
    }

    // 서비스가 popularBook.getBook().getThumbnailUrl() 을 읽으므로 Book 목까지 연결해 둔다
    private PopularBook popularBookWithThumbnail(String thumbnailKey) {
        Book book = mock(Book.class);
        when(book.getThumbnailUrl()).thenReturn(thumbnailKey);
        PopularBook popularBook = mock(PopularBook.class);
        when(popularBook.getBook()).thenReturn(book);
        return popularBook;
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
