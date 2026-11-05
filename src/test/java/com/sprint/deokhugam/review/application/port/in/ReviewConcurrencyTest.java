package com.sprint.deokhugam.review.application.port.in;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.sprint.deokhugam.book.application.port.out.BookRepositoryPort;
import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewCreateRequest;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewDto;
import com.sprint.deokhugam.review.application.port.out.ReviewRepositoryPort;
import com.sprint.deokhugam.user.application.port.out.UserRepositoryPort;
import com.sprint.deokhugam.user.domain.User;

/**
 * 리뷰 생성/삭제 시 Book.addReview / removeReview 동시성 테스트.
 * 트랜잭션 경계를 실제로 타야 하므로 @Transactional 없이 실제 DB(Docker postgres)를 사용한다.
 */
@SpringBootTest
class ReviewConcurrencyTest {

    // 동시에 요청을 보낼 스레드(= 유저) 수
    private static final int THREAD_COUNT = 10;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepositoryPort reviewRepositoryPort;

    @Autowired
    private BookRepositoryPort bookRepositoryPort;

    @Autowired
    private UserRepositoryPort userRepositoryPort;

    private Book book;          // 모든 스레드가 리뷰를 남기는 대상 도서 (경합이 일어나는 공유 자원)
    private List<User> users;   // 리뷰 작성자들 (도서당 유저 1리뷰 제약 때문에 스레드마다 다른 유저 사용)

    // 테스트마다 도서 1권 + 유저 THREAD_COUNT명을 DB에 미리 저장해 둔다.
    @BeforeEach
    void setUp() {
        User owner = userRepositoryPort.save(User.create("owner@test.com", "owner", "pw1234567"));
        book = bookRepositoryPort.save(Book.create(
            "동시성 도서", owner, "저자", "설명", "출판사", LocalDate.of(2024, 1, 1), "9780000000001", null));

        users = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            users.add(userRepositoryPort.save(User.create("user" + i + "@test.com", "user" + i, "pw1234567")));
        }
    }

    // @Transactional 롤백을 쓰지 않으므로 직접 정리한다. (FK 순서: 리뷰 → 도서 → 유저)
    @AfterEach
    void tearDown() {
        // deleteAll : 조회 후 삭제한다.
        // deleteAllInBatch : 대량 삭제 (바로 삭제한다.)
        reviewRepositoryPort.deleteAllInBatch();
        bookRepositoryPort.deleteAllInBatch();
        userRepositoryPort.deleteAllInBatch();
    }

    /*
     * [테스트 1] 동시 리뷰 생성
     * 유저 10명이 같은 도서에 동시에 리뷰를 작성한다.
     * 락이 없으면 여러 트랜잭션이 같은 reviewCount/rating 값을 읽고 덮어써서(lost update)
     * 저장된 리뷰 수보다 book.reviewCount가 작아진다.
     * 검증: 저장된 리뷰 수 == book.reviewCount, 평균 평점 == 3.0
     */
    @Test
    @DisplayName("서로 다른 유저가 동시에 리뷰를 생성해도 reviewCount와 평균 평점이 정확해야 한다")
    void createReview_concurrently_keepsReviewCountAndRating() throws InterruptedException {
        // given: 유저 i 는 (i % 5) + 1 점을 준다 → 평균 3.0
        AtomicInteger failCount = new AtomicInteger();

        // when
        runConcurrently(THREAD_COUNT, i -> {
            try {
                reviewService.createReview(new ReviewCreateRequest(
                    book.getId(), users.get(i).getId(), "내용" + i, (i % 5) + 1));
            } catch (Exception e) {
                failCount.incrementAndGet();
            }
        });

        // then
        Book result = bookRepositoryPort.findById(book.getId()).orElseThrow();
        long savedReviews = reviewRepositoryPort.count();

        assertThat(savedReviews).isEqualTo(THREAD_COUNT - failCount.get());
        assertThat(result.getReviewCount()).isEqualTo((int)savedReviews);
        if (failCount.get() == 0) {
            assertThat(result.getRating()).isCloseTo(3.0, within(0.0001));
        }
    }

    /*
     * [테스트 2] 동시 리뷰 삭제
     * 리뷰 10개를 순차로 만들어 둔 뒤, 그중 5개를 동시에 soft delete 한다.
     * 락이 없으면 removeReview 계산이 서로 덮어써져 reviewCount가 5가 되지 않는다.
     * 검증: book.reviewCount == 10 - 삭제 성공 수, 평균 평점 == 3.0 (남은 5개가 1~5점)
     */
    @Test
    @DisplayName("여러 리뷰를 동시에 삭제해도 reviewCount와 평균 평점이 정확해야 한다")
    void deleteReview_concurrently_keepsReviewCountAndRating() throws InterruptedException {
        // given: 유저 i 가 (i % 5) + 1 점 리뷰를 순차 생성
        List<ReviewDto> reviews = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            reviews.add(reviewService.createReview(new ReviewCreateRequest(
                book.getId(), users.get(i).getId(), "내용" + i, (i % 5) + 1)));
        }
        Book before = bookRepositoryPort.findById(book.getId()).orElseThrow();
        assertThat(before.getReviewCount()).isEqualTo(THREAD_COUNT);

        // when: 앞의 절반(i = 0..4, 점수 1~5)을 동시에 삭제 → 남는 건 i = 5..9 (점수 1~5, 평균 3.0)
        int deleteCount = THREAD_COUNT / 2;
        AtomicInteger failCount = new AtomicInteger();
        runConcurrently(deleteCount, i -> {
            try {
                reviewService.reviewSoftDelete(reviews.get(i).id(), users.get(i).getId());
            } catch (Exception e) {
                failCount.incrementAndGet();
            }
        });

        // then
        Book result = bookRepositoryPort.findById(book.getId()).orElseThrow();
        int deleted = deleteCount - failCount.get();

        assertThat(result.getReviewCount()).isEqualTo(THREAD_COUNT - deleted);
        if (failCount.get() == 0) {
            assertThat(result.getRating()).isCloseTo(3.0, within(0.0001));
        }
    }

    /*
     * [테스트 3] 생성 + 삭제 혼합
     * 기존 리뷰 5개를 삭제하는 스레드 5개와, 새 리뷰 5개를 만드는 스레드 5개를 동시에 실행한다.
     * addReview와 removeReview가 서로 섞여도 최종 reviewCount는 5 - 5 + 5 = 5 여야 한다.
     * 검증: book.reviewCount == 5, 평균 평점 == 3.0 (모두 3점)
     */
    @Test
    @DisplayName("리뷰 생성과 삭제가 동시에 일어나도 reviewCount가 정확해야 한다")
    void createAndDelete_concurrently_keepsReviewCount() throws InterruptedException {
        // given: 유저 0..4 는 이미 리뷰 보유, 유저 5..9 는 아직 없음
        int half = THREAD_COUNT / 2;
        List<ReviewDto> existing = new ArrayList<>();
        for (int i = 0; i < half; i++) {
            existing.add(reviewService.createReview(new ReviewCreateRequest(
                book.getId(), users.get(i).getId(), "기존" + i, 3)));
        }

        // when: 5개는 삭제, 5개는 생성을 동시에 수행 (모두 3점이라 평균은 항상 3.0)
        AtomicInteger failCount = new AtomicInteger();
        runConcurrently(THREAD_COUNT, i -> {
            try {
                if (i < half) {
                    reviewService.reviewSoftDelete(existing.get(i).id(), users.get(i).getId());
                } else {
                    reviewService.createReview(new ReviewCreateRequest(
                        book.getId(), users.get(i).getId(), "신규" + i, 3));
                }
            } catch (Exception e) {
                failCount.incrementAndGet();
            }
        });

        // then: 5 - 5 + 5 = 5
        Book result = bookRepositoryPort.findById(book.getId()).orElseThrow();
        if (failCount.get() == 0) {
            assertThat(result.getReviewCount()).isEqualTo(half);
            assertThat(result.getRating()).isCloseTo(3.0, within(0.0001));
        }
    }

    /**
     * threads 개의 스레드를 만들어 task 를 동시에 실행하고, 모두 끝날 때까지 기다린다.
     * - ready : 모든 스레드가 준비될 때까지 메인 스레드가 대기
     * - start : 준비가 끝나면 한 번에 풀어줘서 동시에 출발 (경합 최대화)
     * - done  : 모든 스레드가 끝났는지 확인 (30초 안에 안 끝나면 실패)
     * task 에는 스레드 번호(0..threads-1)가 전달된다.
     */
    private void runConcurrently(int threads, IndexedTask task) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            final int index = i;
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    task.run(index);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();
    }

    @FunctionalInterface
    private interface IndexedTask {
        void run(int index);
    }
}
