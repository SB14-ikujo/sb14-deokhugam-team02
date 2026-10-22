package com.sprint.deokhugam.dashboard.application.port.in.popularbook;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.dashboard.application.port.out.popularbook.PopularBookRepositoryPort;
import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PopularBook;
import com.sprint.deokhugam.dashboard.domain.PopularBookRanker;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopularBookRankingService implements PopularBookRankingUseCase {

    private final PopularBookRepositoryPort popularBookRepositoryPort;
    private final PopularBookRanker popularBookRanker = new PopularBookRanker();

    @Override
    @Transactional
    public void rankBooks(LocalDate baseDate) {
        log.info("인기 도서 집계 시작: baseDate={}", baseDate);

        for (PeriodType period : PeriodType.values()) {
            // 같은  날 다시 돌려도 중복되지 않게 기존 순위 지우기
            popularBookRepositoryPort.deleteByPeriodAndSnapshotDate(period, baseDate);

            // 기간 안에 리뷰를 도서별로 모아 후보 만들기
            List<PopularBook> candidates = popularBookRepositoryPort.findCandidates(period, baseDate);

            // 정렬하고 순위 매기기
            List<PopularBook> ranked = popularBookRanker.rank(candidates);

            // 저장
            popularBookRepositoryPort.saveAll(ranked);

            log.info("인기 도서 집계 완료: baseDate={}, period={}, count={}", baseDate, period, ranked.size());
        }
    }
}
