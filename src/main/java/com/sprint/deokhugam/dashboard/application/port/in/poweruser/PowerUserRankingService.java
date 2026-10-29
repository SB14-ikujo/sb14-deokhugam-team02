package com.sprint.deokhugam.dashboard.application.port.in.poweruser;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.dashboard.application.port.out.poweruser.PowerUserRepositoryPort;
import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PowerUser;
import com.sprint.deokhugam.dashboard.domain.PowerUserRanker;

@Slf4j
@Service
@RequiredArgsConstructor
public class PowerUserRankingService implements PowerUserRankingUseCase {
    private final PowerUserRepositoryPort powerUserRepositoryPort;
    private final PowerUserRanker powerUserRanker = new PowerUserRanker();

    @Override
    @Transactional
    public void rankUsers(LocalDate baseDate) {
        log.debug("파워 유저 집계 시작: baseDate={}", baseDate);

        for (PeriodType period : PeriodType.values()) {
            powerUserRepositoryPort.deleteByPeriodAndSnapshotDate(period, baseDate);
            List<PowerUser> candidates = powerUserRepositoryPort.findCandidates(period, baseDate);
            List<PowerUser> rank = powerUserRanker.rank(candidates);
            powerUserRepositoryPort.saveAll(rank);
            log.info("파워 유저 집계 완료: baseDate={}, period={}, count={}", baseDate, period, rank.size());
        }

    }
}
