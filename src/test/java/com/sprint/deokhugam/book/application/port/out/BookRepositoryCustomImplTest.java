package com.sprint.deokhugam.book.application.port.out;

import static org.assertj.core.api.Assertions.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.common.config.JpaConfig;
import com.sprint.deokhugam.common.config.QuerydslConfig;
import com.sprint.deokhugam.user.domain.User;

@DataJpaTest
@Import({JpaConfig.class, QuerydslConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryCustomImplTest {

    // data.sql 더미 데이터와 섞이지 않도록 모든 조회를 이 키워드(저자·ISBN에 포함)로 한정한다
    private static final String TAG = "QDSL";
    private static final Instant BASE = Instant.parse("2026-10-01T00:00:00Z");

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private BookRepositoryPort bookRepository;

    /*
     * 제목   출판일       평점 리뷰수 생성시각  비고
     * Alpha  2020-01-01  3.0  5      +1분
     * Bravo  2021-06-01  4.5  10     +2분
     * Charlie 2021-06-01 4.5  10     +3분      Bravo와 출판일/평점/리뷰수 동점
     * Charlie 2019-03-01 1.0  0      +4분      위 Charlie와 제목 동점
     * Delta  2022-12-31  5.0  20     +5분
     * Zulu   (태그 없음)                        키워드 검색에서 제외되어야 함
     * Deleted (태그 있음, 삭제됨)               조회에서 제외되어야 함
     */
    @BeforeEach
    void setUp() {
        User user = entityManager.persist(User.create("qdsl-book@test.com", "qdsl작성자", "password1!"));

        persistBook(user, "Alpha", LocalDate.of(2020, 1, 1), 3.0, 5, 1, TAG + "저자", TAG + "-ISBN-1", false);
        persistBook(user, "Bravo", LocalDate.of(2021, 6, 1), 4.5, 10, 2, TAG + "저자", TAG + "-ISBN-2", false);
        persistBook(user, "Charlie", LocalDate.of(2021, 6, 1), 4.5, 10, 3, TAG + "저자", TAG + "-ISBN-3", false);
        persistBook(user, "Charlie", LocalDate.of(2019, 3, 1), 1.0, 0, 4, TAG + "저자", TAG + "-ISBN-4", false);
        persistBook(user, "Delta", LocalDate.of(2022, 12, 31), 5.0, 20, 5, TAG + "저자", TAG + "-ISBN-5", false);
        persistBook(user, "Zulu", LocalDate.of(2018, 1, 1), 2.0, 1, 6, "다른저자", "9780000000099", false);
        persistBook(user, "Deleted", LocalDate.of(2017, 1, 1), 2.0, 1, 7, TAG + "저자", TAG + "-ISBN-9", true);

        entityManager.flush();
        entityManager.clear();
    }

    private void persistBook(User user, String title, LocalDate publishedDate, double rating, int reviewCount,
        int minutes, String author, String isbn, boolean deleted) {
        Book book = Book.create(title, user, author, "설명", "출판사", publishedDate, isbn, null);
        ReflectionTestUtils.setField(book, "rating", rating);
        ReflectionTestUtils.setField(book, "reviewCount", reviewCount);
        ReflectionTestUtils.setField(book, "isDeleted", deleted);
        entityManager.persist(book);
        entityManager.flush();
        // createdAt은 Auditing이 채우므로, 정렬/커서 검증을 위해 값을 직접 지정한다
        jdbcTemplate.update("UPDATE books SET created_at = ? WHERE id = ?",
            Timestamp.from(BASE.plusSeconds(minutes * 60L)), book.getId());
    }

    private List<String> titles(List<Book> books) {
        return books.stream().map(Book::getTitle).toList();
    }

    private List<Instant> createdAts(List<Book> books) {
        return books.stream().map(Book::getCreatedAt).toList();
    }

    // 서비스의 cursorExtractor와 동일한 규칙
    private String cursorOf(String orderBy, Book book) {
        return switch (orderBy) {
            case "publishedDate" -> book.getPublishedDate().toString();
            case "rating" -> String.valueOf(book.getRating());
            case "reviewCount" -> String.valueOf(book.getReviewCount());
            default -> book.getTitle();
        };
    }

    // limit개씩 커서로 끝까지 이동하며 모든 페이지를 이어 붙인다
    private List<Book> fetchAllByPaging(String orderBy, Sort.Direction direction, int limit) {
        List<Book> all = new ArrayList<>();
        String cursor = null;
        Instant after = null;
        while (true) {
            List<Book> rows = bookRepository.findAllByCursor(TAG, orderBy, direction, cursor, after, limit);
            List<Book> page = rows.subList(0, Math.min(limit, rows.size()));
            all.addAll(page);
            if (rows.size() <= limit) {
                return all;
            }
            Book last = page.get(page.size() - 1);
            cursor = cursorOf(orderBy, last);
            after = last.getCreatedAt();
        }
    }

    @Test
    @DisplayName("키워드는 제목, 저자, ISBN에서 검색된다")
    void findAllByCursor_keyword() {
        assertThat(titles(bookRepository.findAllByCursor("Delta", "title", Sort.Direction.ASC, null, null, 10)))
            .containsExactly("Delta");                                                    // 제목
        assertThat(bookRepository.findAllByCursor(TAG + "저자", "title", Sort.Direction.ASC, null, null, 10))
            .hasSize(5);                                                                   // 저자
        assertThat(titles(bookRepository.findAllByCursor(TAG + "-ISBN-2", "title", Sort.Direction.ASC, null, null, 10)))
            .containsExactly("Bravo");                                                    // ISBN
    }

    @Test
    @DisplayName("삭제된 도서는 조회와 개수에서 제외된다")
    void findAllByCursor_excludesDeleted() {
        List<Book> books = bookRepository.findAllByCursor(TAG, "title", Sort.Direction.ASC, null, null, 10);

        assertThat(titles(books)).doesNotContain("Deleted", "Zulu");
        assertThat(bookRepository.countByKeyword(TAG)).isEqualTo(5L);
    }

    @Test
    @DisplayName("일치하는 도서가 없으면 빈 목록과 0을 반환한다")
    void noMatch() {
        assertThat(bookRepository.findAllByCursor("존재하지않는키워드", "title", Sort.Direction.ASC, null, null, 10))
            .isEmpty();
        assertThat(bookRepository.countByKeyword("존재하지않는키워드")).isZero();
    }

    @Test
    @DisplayName("limit + 1건을 조회해 다음 페이지 존재 여부를 판단할 수 있다")
    void findAllByCursor_fetchesLimitPlusOne() {
        assertThat(bookRepository.findAllByCursor(TAG, "title", Sort.Direction.ASC, null, null, 2)).hasSize(3);
        assertThat(bookRepository.findAllByCursor(TAG, "title", Sort.Direction.ASC, null, null, 5)).hasSize(5);
    }

    // 동점은 생성 시각으로 구분된다 (같은 방향)
    static Stream<Arguments> orderings() {
        return Stream.of(
            Arguments.of("title", Sort.Direction.ASC, List.of("Alpha", "Bravo", "Charlie", "Charlie", "Delta")),
            Arguments.of("title", Sort.Direction.DESC, List.of("Delta", "Charlie", "Charlie", "Bravo", "Alpha")),
            Arguments.of("publishedDate", Sort.Direction.DESC,
                List.of("Delta", "Charlie", "Bravo", "Alpha", "Charlie")),
            Arguments.of("publishedDate", Sort.Direction.ASC, List.of("Charlie", "Alpha", "Bravo", "Charlie", "Delta")),
            Arguments.of("rating", Sort.Direction.DESC, List.of("Delta", "Charlie", "Bravo", "Alpha", "Charlie")),
            Arguments.of("rating", Sort.Direction.ASC, List.of("Charlie", "Alpha", "Bravo", "Charlie", "Delta")),
            Arguments.of("reviewCount", Sort.Direction.DESC, List.of("Delta", "Charlie", "Bravo", "Alpha", "Charlie")),
            Arguments.of("reviewCount", Sort.Direction.ASC, List.of("Charlie", "Alpha", "Bravo", "Charlie", "Delta"))
        );
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("orderings")
    @DisplayName("정렬 기준과 방향에 맞게 정렬되고, 동점은 생성 시각 순서를 따른다")
    void findAllByCursor_order(String orderBy, Sort.Direction direction, List<String> expectedTitles) {
        List<Book> books = bookRepository.findAllByCursor(TAG, orderBy, direction, null, null, 10);

        assertThat(titles(books)).containsExactlyElementsOf(expectedTitles);
    }

    @Test
    @DisplayName("정렬값이 같은 도서는 생성 시각 방향이 정렬 방향과 같다")
    void findAllByCursor_tieBreakByCreatedAt() {
        List<Book> desc = bookRepository.findAllByCursor(TAG, "rating", Sort.Direction.DESC, null, null, 10);
        List<Book> asc = bookRepository.findAllByCursor(TAG, "rating", Sort.Direction.ASC, null, null, 10);

        // 평점 4.5 동점(Bravo +2분, Charlie +3분)
        assertThat(createdAts(desc.subList(1, 3))).isSortedAccordingTo(java.util.Comparator.reverseOrder());
        assertThat(createdAts(asc.subList(2, 4))).isSorted();
    }

    @ParameterizedTest(name = "{0} {1} limit={2}")
    @MethodSource("pagingCases")
    @DisplayName("커서로 이동하며 조회해도 누락, 중복 없이 한 번에 조회한 순서와 같다")
    void findAllByCursor_pagingMatchesFullOrder(String orderBy, Sort.Direction direction, int limit) {
        List<Book> full = bookRepository.findAllByCursor(TAG, orderBy, direction, null, null, 100);

        List<Book> paged = fetchAllByPaging(orderBy, direction, limit);

        assertThat(paged).extracting(Book::getId)
            .containsExactlyElementsOf(full.stream().map(Book::getId).toList());
        assertThat(paged).extracting(Book::getId).doesNotHaveDuplicates();
    }

    static Stream<Arguments> pagingCases() {
        List<Arguments> cases = new ArrayList<>();
        for (String orderBy : List.of("title", "publishedDate", "rating", "reviewCount")) {
            for (Sort.Direction direction : Sort.Direction.values()) {
                for (int limit : new int[] {1, 2, 3}) {
                    cases.add(Arguments.of(orderBy, direction, limit));
                }
            }
        }
        return cases.stream();
    }

    @Test
    @DisplayName("커서 또는 보조 커서 중 하나라도 없으면 커서 조건을 적용하지 않는다")
    void findAllByCursor_ignoresPartialCursor() {
        List<Book> onlyCursor = bookRepository.findAllByCursor(TAG, "title", Sort.Direction.ASC, "Bravo", null, 10);
        List<Book> onlyAfter = bookRepository.findAllByCursor(TAG, "title", Sort.Direction.ASC, null, BASE, 10);

        assertThat(onlyCursor).hasSize(5);
        assertThat(onlyAfter).hasSize(5);
    }
}
