package com.sprint.deokhugam.book.domain;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sprint.deokhugam.user.domain.User;

class BookTest {

    @Test
    @DisplayName("도서를 생성하면 전달한 값이 올바르게 설정된다.")
    void createBook() {
        // given
        User user = User.create(
            "test@gmail.com",
            "testMok",
            "qwerty1234"
        );

        String title = "원피스 99: 밀짚모자 루피 (밀짚모자 루피)";
        String author = "오다 에이치로";
        String description = "간부들의 맹추격을 동료들이 저지하고, 옥상을 향한 루피는 마침내 카이도에게?! "
            + "큰 무대에 모인 배우들의 격전이 지금, 시작되려고 한다... 오니가시마 결전 최대 히트 업!! "
            + "'원피스'를 둘러싼 해양 모험 로망!!";
        String publisher = "대원씨아이";
        LocalDate publishedDate = LocalDate.of(2023, 8, 16);
        String isbn = "9791168945456";
        String thumbnailUrl = "https://example.com/thumbnail.png";

        // when
        Book book = Book.create(title, user, author, description, publisher, publishedDate, isbn, thumbnailUrl);

        // then
        assertThat(book.getTitle()).isEqualTo(title);
        assertThat(book.getUser()).isSameAs(user);
        assertThat(book.getAuthor()).isEqualTo(author);
        assertThat(book.getDescription()).isEqualTo(description);
        assertThat(book.getPublisher()).isEqualTo(publisher);
        assertThat(book.getPublishedDate()).isEqualTo(publishedDate);
        assertThat(book.getIsbn()).isEqualTo(isbn);
        assertThat(book.getThumbnailUrl()).isEqualTo(thumbnailUrl);
    }

    @Test
    @DisplayName("도서 생성 시 리뷰 수와 평점은 0이고 삭제 상태는 false이다.")
    void createBookWithDefaultValues() {
        // given
        User user = User.create(
            "test@gmail.com",
            "testMok",
            "qwerty1234"
        );

        // when
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

        // then
        assertThat(book.getReviewCount()).isZero();
        assertThat(book.getRating()).isZero();
        assertThat(book.isDeleted()).isFalse();
    }
}
