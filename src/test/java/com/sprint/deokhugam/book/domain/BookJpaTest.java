package com.sprint.deokhugam.book.domain;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.persistence.EntityManager;

import com.sprint.deokhugam.common.config.QuerydslConfig;
import com.sprint.deokhugam.user.domain.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    QuerydslConfig.class,
    BookJpaTest.JpaAuditingTestConfig.class
})
class BookJpaTest {

    @TestConfiguration(proxyBeanMethods = false)
    @EnableJpaAuditing
    static class JpaAuditingTestConfig {

    }

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.create(
            "test@gmail.com",
            "testMok",
            "qwerty1234"
        );

        entityManager.persist(user);
        entityManager.flush();
    }

    @Test
    @DisplayName("도서를 등록한 뒤 조회할 수 있다.")
    void saveAndFindBook() {
        // given
        Book book = Book.create(
            "원피스 99: 밀짚모자 루피 (밀짚모자 루피)",
            user,
            "오다 에이치로",
            "간부들의 맹추격을 동료들이 저지하고, 옥상을 향한 루피는 마침내 카이도에게?! "
                + "큰 무대에 모인 배우들의 격전이 지금, 시작되려고 한다... 오니가시마 결전 최대 히트 업!! "
                + "'원피스'를 둘러싼 해양 모험 로망!!",
            "대원씨아이",
            LocalDate.of(2023, 8, 16),
            "9791168945456",
            null
        );

        // when
        entityManager.persist(book);
        entityManager.flush();

        UUID bookId = book.getId();

        entityManager.clear();

        Book foundBook = entityManager.find(Book.class, bookId);

        // then
        assertThat(foundBook).isNotNull();
        assertThat(foundBook.getTitle()).isEqualTo("원피스 99: 밀짚모자 루피 (밀짚모자 루피)");
        assertThat(foundBook.getUser().getId()).isEqualTo(user.getId());
        assertThat(foundBook.getIsbn()).isEqualTo("9791168945456");
        assertThat(foundBook.getThumbnailUrl()).isNull();
    }

    @Test
    @DisplayName("필수 필드가 누락되면 DB 저장에 실패한다.")
    void failWhenRequiredColumnIsNull() {
        // when & then
        assertThatThrownBy(() -> insertBook(
            null,
            "9791168945456"
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("이미 존재하는 ISBN으로 도서를 등록할 수 없다.")
    void failWhenIsbnDuplicated() {
        // given
        insertBook("원피스 99: 밀짚모자 루피 (밀짚모자 루피)", "9791168945456");

        // when & then
        assertThatThrownBy(() -> insertBook(
            "테스트 테스트지롱",
            "9791168945456"
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("논리 삭제된 도서의 ISBN은 재사용할 수 없다.")
    void failWhenIsbnBelongsToSoftDeletedBook() {
        // given
        String isbn = "9791168945456";

        insertBook("논리 삭제된 도서", isbn, true);

        // when & then
        assertThatThrownBy(() -> insertBook("새로운 도서", isbn))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("물리 삭제된 도서의 ISBN은 재사용할 수 없다.")
    void canReuseIsbnAfterPhysicalDelete() {
        // given
        String isbn = "9791168945456";

        insertBook("물리 삭제된 도서", isbn);

        // when
        jdbcTemplate.update(
            "DELETE FROM books WHERE isbn = ?",
            isbn
        );

        // then
        assertThatCode(() -> insertBook("새로운 도서", isbn))
            .doesNotThrowAnyException();
    }

    private void insertBook(String title, String isbn) {
        insertBook(title, isbn, false);
    }

    private void insertBook(String title, String isbn, boolean isDeleted) {
        String sql = """
            INSERT INTO books (
                id,
                title,
                user_id,
                author,
                description,
                publisher,
                published_date,
                isbn,
                review_count,
                rating,
                is_deleted,
                created_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(
            sql,
            UUID.randomUUID(),
            title,
            user.getId(),
            "테스트 저자",
            "테스트 테스트",
            "테스트 출판사",
            LocalDate.of(2026, 10, 22),
            isbn,
            0,
            0.0,
            isDeleted,
            Timestamp.from(Instant.now())
        );
    }
}
