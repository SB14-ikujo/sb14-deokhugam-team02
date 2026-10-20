package com.sprint.deokhugam.dashboard.application.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularBookDto;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularBookRequest;
import com.sprint.deokhugam.dashboard.adapter.in.web.mapper.DashboardMapper;
import com.sprint.deokhugam.dashboard.application.port.out.book.PopularBookRepositoryPort;
import com.sprint.deokhugam.dashboard.domain.PopularBook;
import com.sprint.deokhugam.dashboard.domain.exception.DashboardErrorCode;
import com.sprint.deokhugam.dashboard.domain.exception.DashboardException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService implements DashboardUseCase {

    private final DashboardMapper dashboardMapper;
    private final PopularBookRepositoryPort popularBookRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<PopularBookDto> getPopularBooks(PopularBookRequest request) {
        log.debug("인기 도서 조회 요청: period={}, direction={}, cursor={}, limit={}",
            request.period(), request.direction(), request.cursor(), request.limit());

        // 최신 스냅샷 날짜 찾기
        Optional<LocalDate> latestSnapshotDate = popularBookRepositoryPort.findLatestSnapshotDate(request.period());

        if (latestSnapshotDate.isEmpty()) {
            log.info("인기 도서 스냅샷 없음: period={}", request.period());
            return new CursorPageResponse<>(List.of(), null, null, 0, 0L, false);
        }
        LocalDate snapshotDate = latestSnapshotDate.get();

        // 한 페이지 가져오기
        int limit = request.limit();
        List<PopularBook> rows = popularBookRepositoryPort.findPage(request.period(), snapshotDate,
            request.direction(), parseCursor(request.cursor()), limit + 1);

        // 다음 페이지 판단 + DTO 변환
        boolean hasNext = rows.size() > limit;
        List<PopularBook> page = hasNext ? rows.subList(0, limit) : rows;
        List<PopularBookDto> content = page.stream()
            .map(dashboardMapper::toDto)
            .toList();

        // 응답 만들기
        long total = popularBookRepositoryPort.countByPeriodAndSnapshotDate(request.period(), snapshotDate);

        if (!hasNext) {
            return new CursorPageResponse<>(content, null, null, content.size(), total, false);
        }
        PopularBook last = page.get(page.size() - 1);

        log.debug("인기 도서 조회 완료: snapshotDate={}, size={}, total={}",
            snapshotDate, content.size(), total);

        return new CursorPageResponse<>(content, String.valueOf(last.getRank()), last.getCreatedAt(),
            content.size(), total, true);

    }

    //커서 변환
    private Long parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(cursor);
        } catch (NumberFormatException ex) {
            throw new DashboardException(DashboardErrorCode.DASHBOARD_INVALID_CURSOR);
        }
    }
}
