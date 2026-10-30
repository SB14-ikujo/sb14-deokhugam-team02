package com.sprint.deokhugam.dashboard.application.port.in.poweruser;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sprint.deokhugam.dashboard.application.port.out.poweruser.PowerUserRepositoryPort;
import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PowerUser;
import com.sprint.deokhugam.user.domain.User;

@ExtendWith(MockitoExtension.class)
class PowerUserRankingServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);

    @Mock
    private PowerUserRepositoryPort powerUserRepositoryPort;

    @InjectMocks
    private PowerUserRankingService powerUserRankingService;

    @Test
    @DisplayName("기간마다 기존 순위를 지우고, 후보를 조회해 저장하는 순서로 집계한다")
    void rankUsersForEveryPeriodInOrder() {
        powerUserRankingService.rankUsers(TODAY);

        InOrder inOrder = inOrder(powerUserRepositoryPort);
        for (PeriodType period : PeriodType.values()) {
            inOrder.verify(powerUserRepositoryPort).deleteByPeriodAndSnapshotDate(period, TODAY);
            inOrder.verify(powerUserRepositoryPort).findCandidates(period, TODAY);
            inOrder.verify(powerUserRepositoryPort).saveAll(List.of());
        }
    }

    @Test
    @DisplayName("후보에 순위를 매겨 점수 높은 순으로 저장한다")
    void rankUsersSavesRankedCandidates() {
        PowerUser lowScore = candidate("userA", 1, 0.7);
        PowerUser highScore = candidate("userB", 2, 2.3);
        when(powerUserRepositoryPort.findCandidates(PeriodType.DAILY, TODAY))
            .thenReturn(List.of(lowScore, highScore));

        powerUserRankingService.rankUsers(TODAY);

        assertThat(highScore.getRank()).isEqualTo(1L);
        assertThat(lowScore.getRank()).isEqualTo(2L);
        verify(powerUserRepositoryPort).saveAll(List.of(highScore, lowScore));
    }

    @Test
    @DisplayName("모든 기간에서 저장한 순위 수를 합해 반환한다")
    void rankUsersReturnsTotalSavedCount() {
        when(powerUserRepositoryPort.findCandidates(PeriodType.DAILY, TODAY))
            .thenReturn(List.of(candidate("userA", 1, 0.7), candidate("userB", 2, 2.3)));
        when(powerUserRepositoryPort.findCandidates(PeriodType.WEEKLY, TODAY))
            .thenReturn(List.of(candidate("userC", 3, 1.5)));

        int total = powerUserRankingService.rankUsers(TODAY);

        assertThat(total).isEqualTo(3);
    }

    private PowerUser candidate(String nickname, long idNumber, double score) {
        User user = User.create(nickname + "@test.com", nickname, "password1!");
        ReflectionTestUtils.setField(user, "id", new UUID(0L, idNumber));
        return PowerUser.create(user, PeriodType.DAILY, null, score, 1.0, 1L, 1L, TODAY);
    }
}
