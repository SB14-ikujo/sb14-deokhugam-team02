package com.sprint.deokhugam.dashboard.application.port.out.book;

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

}
