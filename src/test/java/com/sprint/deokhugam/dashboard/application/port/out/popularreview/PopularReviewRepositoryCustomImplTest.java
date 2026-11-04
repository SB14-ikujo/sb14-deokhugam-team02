package com.sprint.deokhugam.dashboard.application.port.out.popularreview;

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

import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.comment.domain.Comment;
import com.sprint.deokhugam.common.config.JpaConfig;
import com.sprint.deokhugam.common.config.QuerydslConfig;
import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PopularReview;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.review.domain.ReviewLike;
import com.sprint.deokhugam.user.domain.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, QuerydslConfig.class})
class PopularReviewRepositoryCustomImplTest {

    private static final LocalDate YESTERDAY = LocalDate.of(2026, 10, 18);
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    @Autowired
    private PopularReviewRepositoryPort popularReviewRepositoryPort;

    @Autowired
    private TestEntityManager em;

    private Book book;

    private int userSeq;

    @BeforeEach
    void setUp() {
        User owner = saveUser();
        book = Book.create("제목", owner, "저자", "설명", "출판사", LocalDate.of(2020, 1, 1), "9870000000001", null);
        em.persist(book);
    }

    @Test
    @DisplayName("좋아요와 댓글을 함께 가진 리뷰도 각각의 개수를 곱하지 않고 센다")
    void findCandidatesCountsLikesAndCommentsSeparately() {
        Review review = saveReview("A");
        saveLike(review, at(18, 10, 0));
        saveLike(review, at(18, 11, 0));
        saveLike(review, at(18, 12, 0));
        saveComment(review, at(18, 13, 0), false);
        saveComment(review, at(18, 14, 0), false);
        em.clear();

        List<PopularReview> result = popularReviewRepositoryPort.findCandidates(PeriodType.DAILY, TODAY);

        assertThat(result)
            .extracting(candidate -> candidate.getReview().getContent(), PopularReview::getLikeCount,
                PopularReview::getCommentCount)
            .containsExactly(tuple("A", 3L, 2L));
        assertThat(result.get(0).getScore()).isCloseTo(2.3, within(0.000001));
        assertThat(result).allSatisfy(candidate -> {
            assertThat(candidate.getRank()).isNull();
            assertThat(candidate.getPeriod()).isEqualTo(PeriodType.DAILY);
            assertThat(candidate.getSnapshotDate()).isEqualTo(TODAY);
        });
    }

    @Test
    @DisplayName("좋아요나 댓글 중 한쪽만 있는 리뷰는 없는 쪽을 0으로 채우고, 둘 다 없는 리뷰는 제외한다")
    void findCandidatesFillsMissingSideWithZero() {
        Review likedOnly = saveReview("좋아요만");
        Review commentedOnly = saveReview("댓글만");
        saveReview("반응 없음");
        saveLike(likedOnly, at(18, 10, 0));
        saveComment(commentedOnly, at(18, 10, 0), false);
        saveComment(commentedOnly, at(18, 11, 0), false);
        em.clear();

        List<PopularReview> result = popularReviewRepositoryPort.findCandidates(PeriodType.DAILY, TODAY);

        assertThat(result)
            .extracting(candidate -> candidate.getReview().getContent(), PopularReview::getLikeCount,
                PopularReview::getCommentCount)
            .containsExactlyInAnyOrder(tuple("좋아요만", 1L, 0L), tuple("댓글만", 0L, 2L));
    }

    @Test
    @DisplayName("기간 시작 시각은 포함하고, 끝 시각과 시작 이전의 좋아요·댓글은 제외한다")
    void findCandidatesIncludesStartExcludesEnd() {
        Review review = saveReview("A");
        saveLike(review, at(18, 0, 0));
        saveLike(review, at(17, 23, 59));
        saveLike(review, at(19, 0, 0));
        saveComment(review, at(18, 0, 0), false);
        saveComment(review, at(17, 23, 59), false);
        saveComment(review, at(19, 0, 0), false);
        em.clear();

        List<PopularReview> result = popularReviewRepositoryPort.findCandidates(PeriodType.DAILY, TODAY);

        assertThat(result)
            .extracting(PopularReview::getLikeCount, PopularReview::getCommentCount)
            .containsExactly(tuple(1L, 1L));
    }

    @Test
    @DisplayName("리뷰 작성 시각이 기간 밖이어도 기간 안의 좋아요·댓글로 집계한다")
    void findCandidatesUsesReactionTimeNotReviewTime() {
        Review oldReview = saveReview("오래된 리뷰");
        updateCreatedAt("reviews", oldReview.getId(), at(1, 9, 0));
        saveLike(oldReview, at(18, 10, 0));
        em.clear();

        List<PopularReview> result = popularReviewRepositoryPort.findCandidates(PeriodType.DAILY, TODAY);

        assertThat(result)
            .extracting(candidate -> candidate.getReview().getContent(), PopularReview::getLikeCount)
            .containsExactly(tuple("오래된 리뷰", 1L));
    }

    @Test
    @DisplayName("논리 삭제된 댓글도 집계에 포함한다")
    void findCandidatesIncludesDeletedComments() {
        Review review = saveReview("A");
        saveComment(review, at(18, 10, 0), false);
        saveComment(review, at(18, 11, 0), true);
        em.clear();

        List<PopularReview> result = popularReviewRepositoryPort.findCandidates(PeriodType.DAILY, TODAY);

        assertThat(result)
            .extracting(PopularReview::getCommentCount)
            .containsExactly(2L);
    }

    @Test
    @DisplayName("역대 기간은 시작 시각 제한 없이 집계한다")
    void findCandidatesAllTimeHasNoStart() {
        Review review = saveReview("A");
        saveLike(review, at(1, 9, 0));
        em.clear();

        assertThat(popularReviewRepositoryPort.findCandidates(PeriodType.DAILY, TODAY)).isEmpty();
        assertThat(popularReviewRepositoryPort.findCandidates(PeriodType.ALL_TIME, TODAY)).hasSize(1);
    }

    @Test
    @DisplayName("지정한 날짜와 기간의 순위만 지운다")
    void deleteByPeriodAndSnapshotDate() {
        Review review = saveReview("A");
        em.persist(PopularReview.create(review, PeriodType.DAILY, 1L, 2.3, 3L, 2L, TODAY));
        em.persist(PopularReview.create(review, PeriodType.DAILY, 1L, 2.3, 3L, 2L, YESTERDAY));
        em.persist(PopularReview.create(review, PeriodType.WEEKLY, 1L, 2.3, 3L, 2L, TODAY));
        em.flush();
        em.clear();

        popularReviewRepositoryPort.deleteByPeriodAndSnapshotDate(PeriodType.DAILY, TODAY);

        assertThat(popularReviewRepositoryPort.countByPeriodAndSnapshotDate(PeriodType.DAILY, TODAY)).isZero();
        assertThat(popularReviewRepositoryPort.countByPeriodAndSnapshotDate(PeriodType.DAILY, YESTERDAY))
            .isEqualTo(1L);
        assertThat(popularReviewRepositoryPort.countByPeriodAndSnapshotDate(PeriodType.WEEKLY, TODAY))
            .isEqualTo(1L);
    }

    private User saveUser() {
        userSeq++;
        return em.persist(User.create("user" + userSeq + "@test.com", "user" + userSeq, "password1!"));
    }

    private Review saveReview(String content) {
        return em.persist(Review.create(saveUser(), book, content, 5));
    }

    private void saveLike(Review review, Instant createdAt) {
        ReviewLike reviewLike = em.persist(ReviewLike.create(review, saveUser()));
        em.flush();
        updateCreatedAt("review_likes", reviewLike.getId(), createdAt);
    }

    private void saveComment(Review review, Instant createdAt, boolean deleted) {
        Comment comment = Comment.create(review, saveUser(), "댓글");
        if (deleted) {
            comment.softDelete();
        }
        em.persist(comment);
        em.flush();
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
