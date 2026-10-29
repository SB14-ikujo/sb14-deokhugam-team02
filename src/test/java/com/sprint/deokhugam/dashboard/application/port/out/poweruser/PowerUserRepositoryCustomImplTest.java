package com.sprint.deokhugam.dashboard.application.port.out.poweruser;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.comment.domain.Comment;
import com.sprint.deokhugam.common.config.JpaConfig;
import com.sprint.deokhugam.common.config.QuerydslConfig;
import com.sprint.deokhugam.dashboard.domain.Direction;
import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PopularReview;
import com.sprint.deokhugam.dashboard.domain.PowerUser;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.review.domain.ReviewLike;
import com.sprint.deokhugam.user.domain.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, QuerydslConfig.class})
class PowerUserRepositoryCustomImplTest {

    private static final LocalDate YESTERDAY = LocalDate.of(2026, 10, 18);
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    @Autowired
    private PowerUserRepositoryPort powerUserRepositoryPort;

    @Autowired
    private TestEntityManager em;

    private int bookSeq;

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

    @Test
    @DisplayName("지정한 날짜와 기간의 순위만 지운다")
    void deleteByPeriodAndSnapshotDate() {
        powerUserRepositoryPort.deleteByPeriodAndSnapshotDate(PeriodType.DAILY, TODAY);

        assertThat(powerUserRepositoryPort.countByPeriodAndSnapshotDate(PeriodType.DAILY, TODAY)).isZero();
        assertThat(powerUserRepositoryPort.countByPeriodAndSnapshotDate(PeriodType.DAILY, YESTERDAY))
            .isEqualTo(1L);
        assertThat(powerUserRepositoryPort.countByPeriodAndSnapshotDate(PeriodType.WEEKLY, TODAY))
            .isEqualTo(1L);
    }

    @Test
    @DisplayName("작성한 리뷰들의 인기 리뷰 점수를 같은 날짜·기간 것만 더한다")
    void findCandidatesSumsPopularReviewScoreByWriter() {
        User writer = saveUser("W");
        Review first = saveReview(writer);
        Review second = saveReview(writer);
        em.persist(PopularReview.create(first, PeriodType.DAILY, 1L, 3.0, 0L, 0L, TODAY));
        em.persist(PopularReview.create(second, PeriodType.DAILY, 2L, 2.0, 0L, 0L, TODAY));
        // 어제 DAILY, 오늘 WEEKLY: 더하면 안 되는 점수
        em.persist(PopularReview.create(first, PeriodType.DAILY, 1L, 10.0, 0L, 0L, YESTERDAY));
        em.persist(PopularReview.create(first, PeriodType.WEEKLY, 1L, 10.0, 0L, 0L, TODAY));
        em.flush();
        em.clear();

        List<PowerUser> result = powerUserRepositoryPort.findCandidates(PeriodType.DAILY, TODAY);

        assertThat(result)
            .extracting(candidate -> candidate.getUser().getNickname(), PowerUser::getReviewScoreSum,
                PowerUser::getLikeCount, PowerUser::getCommentCount)
            .containsExactly(tuple("userW", 5.0, 0L, 0L));
        assertThat(result).allSatisfy(candidate -> {
            assertThat(candidate.getRank()).isNull();
            assertThat(candidate.getPeriod()).isEqualTo(PeriodType.DAILY);
            assertThat(candidate.getSnapshotDate()).isEqualTo(TODAY);
        });
    }

    @Test
    @DisplayName("좋아요·댓글은 리뷰 작성자가 아니라 누른 사람·쓴 사람 기준으로 각각 세고, 서로 곱하지 않는다")
    void findCandidatesCountsReactionsByActor() {
        User writer = saveUser("W");
        User fan = saveUser("F");
        Review first = saveReview(writer);
        Review second = saveReview(writer);
        saveLike(first, fan, at(18, 10, 0));
        saveLike(second, fan, at(18, 11, 0));
        saveComment(first, fan, at(18, 12, 0));
        saveComment(first, fan, at(18, 13, 0));
        saveComment(second, fan, at(18, 14, 0));
        em.clear();

        List<PowerUser> result = powerUserRepositoryPort.findCandidates(PeriodType.DAILY, TODAY);

        assertThat(result)
            .extracting(candidate -> candidate.getUser().getNickname(), PowerUser::getReviewScoreSum,
                PowerUser::getLikeCount, PowerUser::getCommentCount)
            .containsExactly(tuple("userF", 0.0, 2L, 3L));
    }

    @Test
    @DisplayName("리뷰 점수·좋아요·댓글 중 일부만 있는 사용자는 없는 쪽을 0으로 채운다")
    void findCandidatesFillsMissingSourceWithZero() {
        User writer = saveUser("W");
        User liker = saveUser("L");
        User talker = saveUser("T");
        Review review = saveReview(writer);
        em.persist(PopularReview.create(review, PeriodType.DAILY, 1L, 4.0, 1L, 2L, TODAY));
        em.flush();
        saveLike(review, liker, at(18, 10, 0));
        saveComment(review, talker, at(18, 11, 0));
        saveComment(review, talker, at(18, 12, 0));
        em.clear();

        List<PowerUser> result = powerUserRepositoryPort.findCandidates(PeriodType.DAILY, TODAY);

        assertThat(result)
            .extracting(candidate -> candidate.getUser().getNickname(), PowerUser::getReviewScoreSum,
                PowerUser::getLikeCount, PowerUser::getCommentCount, PowerUser::getScore)
            .containsExactlyInAnyOrder(
                tuple("userW", 4.0, 0L, 0L, 2.0),
                tuple("userL", 0.0, 1L, 0L, 0.2),
                tuple("userT", 0.0, 0L, 2L, 0.6));
    }

    @Test
    @DisplayName("기간 시작 시각은 포함하고, 끝 시각과 시작 이전의 좋아요·댓글은 제외한다")
    void findCandidatesIncludesStartExcludesEnd() {
        User writer = saveUser("W");
        User fan = saveUser("F");
        Review atStart = saveReview(writer);
        Review beforeStart = saveReview(writer);
        Review atEnd = saveReview(writer);
        saveLike(atStart, fan, at(18, 0, 0));
        saveLike(beforeStart, fan, at(17, 23, 59));
        saveLike(atEnd, fan, at(19, 0, 0));
        saveComment(atStart, fan, at(18, 0, 0));
        saveComment(beforeStart, fan, at(17, 23, 59));
        saveComment(atEnd, fan, at(19, 0, 0));
        em.clear();

        List<PowerUser> result = powerUserRepositoryPort.findCandidates(PeriodType.DAILY, TODAY);

        assertThat(result)
            .extracting(PowerUser::getLikeCount, PowerUser::getCommentCount)
            .containsExactly(tuple(1L, 1L));
    }

    @Test
    @DisplayName("역대 기간은 시작 시각 제한 없이 집계한다")
    void findCandidatesAllTimeHasNoStart() {
        User writer = saveUser("W");
        User fan = saveUser("F");
        saveLike(saveReview(writer), fan, at(1, 9, 0));
        em.clear();

        assertThat(powerUserRepositoryPort.findCandidates(PeriodType.DAILY, TODAY)).isEmpty();
        assertThat(powerUserRepositoryPort.findCandidates(PeriodType.ALL_TIME, TODAY)).hasSize(1);
    }

    private User saveUser(String name) {
        return em.persist(User.create("user" + name + "@test.com", "user" + name, "password1!"));
    }

    // 한 사용자는 도서마다 리뷰를 하나만 쓸 수 있어서, 리뷰마다 도서를 새로 만든다
    private Review saveReview(User writer) {
        bookSeq++;
        Book book = em.persist(Book.create("제목" + bookSeq, writer, "저자", "설명", "출판사",
            LocalDate.of(2020, 1, 1), String.format("97800000%05d", bookSeq), null));
        return em.persist(Review.create(writer, book, "내용", 5));
    }

    private void saveLike(Review review, User liker, Instant createdAt) {
        ReviewLike reviewLike = em.persist(ReviewLike.create(review, liker));
        updateCreatedAt("review_likes", reviewLike.getId(), createdAt);
    }

    private void saveComment(Review review, User writer, Instant createdAt) {
        Comment comment = em.persist(Comment.create(review, writer, "댓글"));
        updateCreatedAt("comments", comment.getId(), createdAt);
    }

    // createdAt은 저장할 때 자동으로 채워지므로, 저장 후 SQL로 원하는 시각으로 바꾼다
    private void updateCreatedAt(String table, Object id, Instant createdAt) {
        em.flush();
        em.getEntityManager()
            .createNativeQuery("update " + table + " set created_at = :createdAt where id = :id")
            .setParameter("createdAt", createdAt)
            .setParameter("id", id)
            .executeUpdate();
    }

    private Instant at(int day, int hour, int minute) {
        return LocalDateTime.of(2026, 10, day, hour, minute).atZone(ZONE).toInstant();
    }
}
