package com.sprint.deokhugam.dashboard.application.port.out.popularbook;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.user.domain.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, QuerydslConfig.class})
class PopularBookRepositoryCustomImplTest {

    private static final LocalDate YESTERDAY = LocalDate.of(2026, 10, 18);
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");       // ← 추가

    @Autowired
    private PopularBookRepositoryPort popularBookRepositoryPort;

    @Autowired
    private TestEntityManager em;

    private int reviewerSeq;

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

    @Test
    @DisplayName("기간 안의 리뷰를 도서별로 모아 리뷰 수와 평점 평균을 낸다")
    void findCandidatesAggregatesPerBook() {
        Book bookX = saveBook("X", "9870000000011");
        Book bookY = saveBook("Y", "9870000000012");
        saveReview(bookX, 5, at(18, 10, 0), false);
        saveReview(bookX, 3, at(18, 15, 0), false);
        saveReview(bookY, 4, at(18, 20, 0), false);
        em.clear();

        List<PopularBook> result = popularBookRepositoryPort.findCandidates(PeriodType.DAILY, TODAY);

        assertThat(result)
            .extracting(candidate -> candidate.getBook().getTitle(), PopularBook::getReviewCount,
                PopularBook::getRating)
            .containsExactlyInAnyOrder(tuple("X", 2L, 4.0), tuple("Y", 1L, 4.0));
        assertThat(result).allSatisfy(candidate -> {
            assertThat(candidate.getRank()).isNull();
            assertThat(candidate.getPeriod()).isEqualTo(PeriodType.DAILY);
            assertThat(candidate.getSnapshotDate()).isEqualTo(TODAY);
        });
    }

    @Test
    @DisplayName("기간 시작 시각은 포함하고, 끝 시각과 그 이전 리뷰는 제외한다")
    void findCandidatesIncludesStartExcludesEnd() {
        Book book = saveBook("X", "9870000000011");
        saveReview(book, 5, at(18, 0, 0), false);
        saveReview(book, 1, at(17, 23, 59), false);
        saveReview(book, 1, at(19, 0, 0), false);
        em.clear();

        List<PopularBook> result = popularBookRepositoryPort.findCandidates(PeriodType.DAILY, TODAY);

        assertThat(result)
            .extracting(PopularBook::getReviewCount, PopularBook::getRating)
            .containsExactly(tuple(1L, 5.0));
    }

    @Test
    @DisplayName("논리 삭제된 리뷰도 집계에 포함한다")
    void findCandidatesIncludesDeletedReviews() {
        Book book = saveBook("X", "9870000000011");
        saveReview(book, 5, at(18, 10, 0), false);
        saveReview(book, 1, at(18, 11, 0), true);
        em.clear();

        List<PopularBook> result = popularBookRepositoryPort.findCandidates(PeriodType.DAILY, TODAY);

        assertThat(result)
            .extracting(PopularBook::getReviewCount, PopularBook::getRating)
            .containsExactly(tuple(2L, 3.0));
    }

    @Test
    @DisplayName("역대 기간은 시작 시각 제한 없이 집계한다")
    void findCandidatesAllTimeHasNoStart() {
        Book book = saveBook("X", "9870000000011");
        saveReview(book, 4, at(1, 9, 0), false);
        em.clear();

        assertThat(popularBookRepositoryPort.findCandidates(PeriodType.DAILY, TODAY)).isEmpty();
        assertThat(popularBookRepositoryPort.findCandidates(PeriodType.ALL_TIME, TODAY)).hasSize(1);
    }

    @Test
    @DisplayName("지정한 날짜와 기간의 순위만 지운다")
    void deleteByPeriodAndSnapshotDate() {
        popularBookRepositoryPort.deleteByPeriodAndSnapshotDate(PeriodType.DAILY, TODAY);

        assertThat(popularBookRepositoryPort.countByPeriodAndSnapshotDate(PeriodType.DAILY, TODAY)).isZero();
        assertThat(popularBookRepositoryPort.countByPeriodAndSnapshotDate(PeriodType.DAILY, YESTERDAY))
            .isEqualTo(1L);
        assertThat(popularBookRepositoryPort.countByPeriodAndSnapshotDate(PeriodType.WEEKLY, TODAY))
            .isEqualTo(1L);
    }

    private Book saveBook(String title, String isbn) {
        User user = em.persist(User.create(isbn + "@test.com", "tester" + title, "password1!"));
        Book book = Book.create(title, user, "저자", "설명", "출판사",
            LocalDate.of(2020, 1, 1), isbn, null);
        ReflectionTestUtils.setField(book, "user", user);
        return em.persist(book);
    }

    private void saveReview(Book book, int rating, Instant createdAt, boolean deleted) {
        reviewerSeq++;
        User reviewer = em.persist(User.create("reviewer" + reviewerSeq + "@test.com", "reviewer" + reviewerSeq,
            "password1!"));
        Review review = Review.create(reviewer, book, "내용", rating);
        ReflectionTestUtils.setField(review, "isDeleted", deleted);
        em.persist(review);
        em.flush();

        // createdAt은 저장할 때 자동으로 채워지므로, 저장 후 SQL로 원하는 시각으로 바꾼다
        em.getEntityManager()
            .createNativeQuery("update reviews set created_at = :createdAt where id = :id")
            .setParameter("createdAt", createdAt)
            .setParameter("id", review.getId())
            .executeUpdate();
    }

    private Instant at(int day, int hour, int minute) {
        return LocalDateTime.of(2026, 10, day, hour, minute).atZone(ZONE).toInstant();
    }

}
