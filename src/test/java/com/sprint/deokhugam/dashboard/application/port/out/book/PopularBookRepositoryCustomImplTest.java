package com.sprint.deokhugam.dashboard.application.port.out.book;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.common.config.JpaConfig;
import com.sprint.deokhugam.common.config.QuerydslConfig;
import com.sprint.deokhugam.dashboard.domain.Direction;
import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PopularBook;
import com.sprint.deokhugam.user.domain.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, QuerydslConfig.class})
class PopularBookRepositoryCustomImplTest {

    private static final LocalDate YESTERDAY = LocalDate.of(2026, 10, 18);
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);

    @Autowired
    private PopularBookRepositoryPort popularBookRepositoryPort;

    @Autowired
    private TestEntityManager em;

    @BeforeEach
    void setUp() {
        Book bookA = saveBook("A", "9870000000001");
        Book bookB = saveBook("B", "9870000000002");
        Book bookC = saveBook("C", "9870000000003");
        Book bookD = saveBook("D", "9870000000004");

        // 오늘 DAILY: C 1위, A 2위, D 3위
        em.persist(PopularBook.create(bookC, PeriodType.DAILY, 1L, 9.0, 5L, 4.5, TODAY));
        em.persist(PopularBook.create(bookA, PeriodType.DAILY, 2L, 8.0, 4L, 4.0, TODAY));
        em.persist(PopularBook.create(bookD, PeriodType.DAILY, 3L, 7.0, 3L, 3.5, TODAY));
        // 어제 DAILY, 오늘 WEEKLY: 섞이면 안 되는 데이터
        em.persist(PopularBook.create(bookA, PeriodType.DAILY, 1L, 9.5, 6L, 4.8, YESTERDAY));
        em.persist(PopularBook.create(bookB, PeriodType.WEEKLY, 1L, 9.9, 9L, 5.0, TODAY));
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("기간별 가장 최근 스냅샷 날짜를 찾는다.")
    void findLatestSnapshotDate() {
        assertThat(popularBookRepositoryPort.findLatestSnapshotDate(PeriodType.DAILY)).contains(TODAY);
        assertThat(popularBookRepositoryPort.findLatestSnapshotDate(PeriodType.MONTHLY)).isEmpty();

    }

    @Test
    @DisplayName("최신 날짜의 같은 기간 순위만 오름차순으로 가져온다.")
    void findPageAscending() {
        List<PopularBook> result = popularBookRepositoryPort.findPage(
            PeriodType.DAILY, TODAY, Direction.ASC, null, 10
        );

        assertThat(result).extracting(PopularBook::getRank).containsExactly(1L, 2L, 3L);
        assertThat(result).extracting(popularBook -> popularBook.getBook().getTitle())
            .containsExactly("C", "A", "D");
    }

    @Test
    @DisplayName("커서 다음 순위부터 이어서 가져온다")
    void findPageWithCursor() {
        List<PopularBook> result = popularBookRepositoryPort.findPage(
            PeriodType.DAILY, TODAY, Direction.ASC, 1L, 10);

        assertThat(result).extracting(PopularBook::getRank).containsExactly(2L, 3L);
    }

    @Test
    @DisplayName("내림차순이면 커서보다 작은 순위를 가져온다")
    void findPageDescendingWithCursor() {
        List<PopularBook> result = popularBookRepositoryPort.findPage(
            PeriodType.DAILY, TODAY, Direction.DESC, 3L, 10);

        assertThat(result).extracting(PopularBook::getRank).containsExactly(2L, 1L);
    }

    @Test
    @DisplayName("최신 날짜·기간의 전체 개수를 센다")
    void countByPeriodAndSnapshotDate() {
        assertThat(popularBookRepositoryPort.countByPeriodAndSnapshotDate(PeriodType.DAILY, TODAY))
            .isEqualTo(3L);
    }

    private Book saveBook(String title, String isbn) {
        User user = em.persist(User.create(isbn + "@test.com", "tester" + title, "password1!"));
        Book book = Book.create(title, "저자", "설명", "출판사",
            LocalDate.of(2020, 1, 1), isbn, null);
        ReflectionTestUtils.setField(book, "user", user);
        return em.persist(book);
    }

}
