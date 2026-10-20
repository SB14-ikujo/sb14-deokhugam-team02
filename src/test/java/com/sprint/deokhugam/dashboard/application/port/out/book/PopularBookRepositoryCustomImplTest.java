package com.sprint.deokhugam.dashboard.application.port.out.book;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.common.config.JpaConfig;
import com.sprint.deokhugam.common.config.QuerydslConfig;
import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PopularBook;

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
        em.persist(PopularBook.create(bookC, PeriodType.DAILY, 2L, 8.0, 4L, 4.0, TODAY));
        em.persist(PopularBook.create(bookC, PeriodType.DAILY, 3L, 7.0, 3L, 3.5, TODAY));
        // 어제 DAILY, 오늘 WEEKLY: 섞이면 안 되는 데이터
        em.persist(PopularBook.create(bookC, PeriodType.DAILY, 1L, 9.5, 6L, 4.8, YESTERDAY));
        em.persist(PopularBook.create(bookC, PeriodType.DAILY, 1L, 9.9, 9L, 5.0, TODAY));
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("기간별 가장 최근 스냅샷 날짜를 찾는다.")
    void findLatestSnapshotDate() {

    }

    private Book saveBook(String title, String isbn) {
        Book book = Book.create(title, "저자", "설명", "출판사",
            LocalDate.of(2020, 1, 1), isbn, null);
        return em.persist(book);
    }

}
