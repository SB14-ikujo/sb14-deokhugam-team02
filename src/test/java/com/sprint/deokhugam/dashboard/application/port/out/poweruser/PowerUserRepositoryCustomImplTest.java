package com.sprint.deokhugam.dashboard.application.port.out.poweruser;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.sprint.deokhugam.common.config.JpaConfig;
import com.sprint.deokhugam.common.config.QuerydslConfig;
import com.sprint.deokhugam.dashboard.domain.Direction;
import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PowerUser;
import com.sprint.deokhugam.user.domain.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, QuerydslConfig.class})
class PowerUserRepositoryCustomImplTest {

    private static final LocalDate YESTERDAY = LocalDate.of(2026, 10, 18);
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);

    @Autowired
    private PowerUserRepositoryPort powerUserRepositoryPort;

    @Autowired
    private TestEntityManager em;

    @BeforeEach
    void setUp() {
        User userA = saveUser("A");
        User userB = saveUser("B");
        User userC = saveUser("C");
        User userD = saveUser("D");

        // 오늘 DAILY: C 1위, A 2위, D 3위
        em.persist(PowerUser.create(userC, PeriodType.DAILY, 1L, 9.0, 5.0, 3L, 2L, TODAY));
        em.persist(PowerUser.create(userA, PeriodType.DAILY, 2L, 8.0, 4.0, 2L, 2L, TODAY));
        em.persist(PowerUser.create(userD, PeriodType.DAILY, 3L, 7.0, 3.0, 1L, 2L, TODAY));
        // 어제 DAILY, 오늘 WEEKLY: 섞이면 안 되는 데이터
        em.persist(PowerUser.create(userA, PeriodType.DAILY, 1L, 9.5, 6.0, 3L, 3L, YESTERDAY));
        em.persist(PowerUser.create(userB, PeriodType.WEEKLY, 1L, 9.9, 7.0, 4L, 4L, TODAY));
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("기간별 가장 최근 스냅샷 날짜를 찾는다")
    void findLatestSnapshotDate() {
        assertThat(powerUserRepositoryPort.findLatestSnapshotDate(PeriodType.DAILY)).contains(TODAY);
        assertThat(powerUserRepositoryPort.findLatestSnapshotDate(PeriodType.MONTHLY)).isEmpty();
    }

    @Test
    @DisplayName("같은 날짜·기간의 순위만 오름차순으로 가져온다")
    void findPageAscending() {
        List<PowerUser> result = powerUserRepositoryPort.findPage(PeriodType.DAILY, TODAY, Direction.ASC, null, 10);

        assertThat(result).extracting(PowerUser::getRank).containsExactly(1L, 2L, 3L);
        assertThat(result).extracting(powerUser -> powerUser.getUser().getNickname())
            .containsExactly("userC", "userA", "userD");
    }

    @Test
    @DisplayName("사용자를 함께 조회해 닉네임을 꺼낼 때 추가 쿼리가 나가지 않는다")
    void findPageFetchesUser() {
        List<PowerUser> result = powerUserRepositoryPort.findPage(PeriodType.DAILY, TODAY, Direction.ASC, null, 10);

        assertThat(result).allSatisfy(powerUser -> assertThat(Hibernate.isInitialized(powerUser.getUser())).isTrue());
    }

    @Test
    @DisplayName("오름차순이면 커서보다 큰 순위부터 가져온다")
    void findPageWithCursor() {
        List<PowerUser> result = powerUserRepositoryPort.findPage(PeriodType.DAILY, TODAY, Direction.ASC, 1L, 10);

        assertThat(result).extracting(PowerUser::getRank).containsExactly(2L, 3L);
    }

    @Test
    @DisplayName("내림차순이면 커서보다 작은 순위를 가져온다")
    void findPageDescendingWithCursor() {
        List<PowerUser> result = powerUserRepositoryPort.findPage(PeriodType.DAILY, TODAY, Direction.DESC, 3L, 10);

        assertThat(result).extracting(PowerUser::getRank).containsExactly(2L, 1L);
    }

    @Test
    @DisplayName("요청한 개수만큼만 가져온다")
    void findPageLimitsSize() {
        List<PowerUser> result = powerUserRepositoryPort.findPage(PeriodType.DAILY, TODAY, Direction.ASC, null, 2);

        assertThat(result).extracting(PowerUser::getRank).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("같은 날짜·기간의 전체 개수를 센다")
    void countByPeriodAndSnapshotDate() {
        assertThat(powerUserRepositoryPort.countByPeriodAndSnapshotDate(PeriodType.DAILY, TODAY)).isEqualTo(3L);
    }

    private User saveUser(String name) {
        return em.persist(User.create("user" + name + "@test.com", "user" + name, "password1!"));
    }
}
