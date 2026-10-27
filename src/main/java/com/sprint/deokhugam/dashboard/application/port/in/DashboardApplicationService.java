package com.sprint.deokhugam.dashboard.application.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.common.util.SliceUtils;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularBookDto;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularBookRequest;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularReviewDto;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularReviewRequest;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PowerUserDto;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PowerUserRequest;
import com.sprint.deokhugam.dashboard.adapter.in.web.mapper.DashboardMapper;
import com.sprint.deokhugam.dashboard.application.port.out.popularbook.PopularBookRepositoryPort;
import com.sprint.deokhugam.dashboard.application.port.out.popularreview.PopularReviewRepositoryPort;
import com.sprint.deokhugam.dashboard.application.port.out.poweruser.PowerUserRepositoryPort;
import com.sprint.deokhugam.dashboard.domain.PopularBook;
import com.sprint.deokhugam.dashboard.domain.PopularReview;
import com.sprint.deokhugam.dashboard.domain.PowerUser;
import com.sprint.deokhugam.dashboard.domain.exception.DashboardErrorCode;
import com.sprint.deokhugam.dashboard.domain.exception.DashboardException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardApplicationService implements DashboardUseCase {

    private final DashboardMapper dashboardMapper;
    private final PopularBookRepositoryPort popularBookRepositoryPort;
    private final PopularReviewRepositoryPort popularReviewRepositoryPort;
    private final PowerUserRepositoryPort powerUserRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<PopularBookDto> getPopularBooks(PopularBookRequest request) {

        // 디버깅 찍기
        log.debug("인기 도서 조회 요청: period={}, direction={}, cursor={}, limit={}",
            request.period(), request.direction(), request.cursor(), request.limit());

        // 최신 스냅샷 날짜 찾기
        Optional<LocalDate> latestSnapshotDate = popularBookRepositoryPort.findLatestSnapshotDate(request.period());

        // 만약 그 기간으 데이터가 아예 하나도 없으면 다 0이나null 보내기
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
        Slice<PopularBookDto> slice = SliceUtils.toSlice(rows, limit).map(dashboardMapper::toDto);

        // 응답 만들기
        long total = popularBookRepositoryPort.countByPeriodAndSnapshotDate(request.period(), snapshotDate);

        log.debug("인기 도서 조회 완료: snapshotDate={}, size={}, total={}",
            snapshotDate, slice.getNumberOfElements(), total);

        return CursorPageResponse.of(slice, total, dto -> String.valueOf(dto.rank()), PopularBookDto::createdAt);

    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<PopularReviewDto> getPopularReviews(PopularReviewRequest request) {
        log.debug("인기 리뷰 조회: period={}, direction={}, cursor={}, limit={}",
            request.period(), request.direction(), request.cursor(), request.limit());

        // popular_review에서
        Optional<LocalDate> latestSnapshotDate = popularReviewRepositoryPort.findLatestSnapshotDate(request.period());

        if (latestSnapshotDate.isEmpty()) {
            log.info("인기 리뷰 스냅샷 없음 : period = {}", request.period());
            return new CursorPageResponse<>(List.of(), null, null, 0, 0L, false);
        }

        LocalDate snapshotDate = latestSnapshotDate.get();
        int limit = request.limit();

        List<PopularReview> rows = popularReviewRepositoryPort.findPage(request.period(), snapshotDate,
            request.direction(), parseCursor(request.cursor()), limit + 1);

        Slice<PopularReviewDto> slice = SliceUtils.toSlice(rows, limit).map(dashboardMapper::toDto);

        long total = popularReviewRepositoryPort.countByPeriodAndSnapshotDate(request.period(), snapshotDate);

        log.debug("인기 리뷰 조회 완료: snapshotDate={}, size={}, total={}",
            snapshotDate, slice.getNumberOfElements(), total);

        return CursorPageResponse.of(slice, total, dto -> String.valueOf(dto.rank()), PopularReviewDto::createdAt);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<PowerUserDto> getPowerUsers(PowerUserRequest request) {
        log.debug("파워 유저 조회: period={}, direction={}, cursor={}, limit={}", request.period(), request.direction(),
            request.cursor(), request.limit());

        Optional<LocalDate> latestSnapshotDate = powerUserRepositoryPort.findLatestSnapshotDate(request.period());

        if (latestSnapshotDate.isEmpty()) {
            log.info("파워 유저 스냅샷 없음: period={}", request.period());
            return new CursorPageResponse<>(List.of(), null, null, 0, 0L, false);
        }
        LocalDate snapshotDate = latestSnapshotDate.get();
        int limit = request.limit();

        List<PowerUser> rows = powerUserRepositoryPort.findPage(request.period(), snapshotDate,
            request.direction(), parseCursor(request.cursor()), limit + 1);

        Slice<PowerUserDto> slice = SliceUtils.toSlice(rows, limit).map(dashboardMapper::toDto);

        long total = powerUserRepositoryPort.countByPeriodAndSnapshotDate(request.period(), snapshotDate);

        log.debug("파워 유저 조회 완료: snapshotDate={}, size={}, total={}",
            snapshotDate, slice.getNumberOfElements(), total);

        return CursorPageResponse.of(slice, total, dto -> String.valueOf(dto.rank()), PowerUserDto::createdAt);
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
