package com.sprint.deokhugam.dashboard.application.port.out.popularbook;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.sprint.deokhugam.dashboard.domain.Direction;
import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PopularBook;

public interface PopularBookRepositoryCustom {

    //스냅샷 날짜 뽑아오기
    Optional<LocalDate> findLatestSnapshotDate(PeriodType period);

    // 그걸로 인기 책 뽑아오기
    List<PopularBook> findPage(PeriodType period, LocalDate snapshotDate, Direction direction,
        Long cursorRank, int size);

    // 기준일, 기간의 리뷰를 도서별로 모아서 순위 없는 후보로 만들기
    List<PopularBook> findCandidates(PeriodType period, LocalDate baseDate);

}
