package com.sprint.deokhugam.book.application.port.out;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sprint.deokhugam.book.domain.Book;

public interface BookRepositoryPort extends JpaRepository<Book, UUID>, BookRepositoryCustom {
    Optional<Book> findByIdAndIsDeletedFalse(UUID id);

    boolean existsByIsbn(String isbn);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Book b
            SET b.rating = (b.rating * b.reviewCount + :grade) / (b.reviewCount + 1),
                b.reviewCount = b.reviewCount + 1
            WHERE b.id = :bookId
        """)
    int addReview(@Param("bookId") UUID bookId, @Param("grade") int grade);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        UPDATE Book b
        SET b.rating = CASE
                WHEN b.reviewCount <= 1 THEN 0.0
                ELSE (b.rating * b.reviewCount - :grade) / (b.reviewCount - 1)
            END,
            b.reviewCount = CASE
                WHEN b.reviewCount <= 1 THEN 0
                ELSE b.reviewCount - 1
            END
        WHERE b.id = :bookId
        """)
    int removeReview(@Param("bookId") UUID bookId, @Param("grade") int grade);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        UPDATE Book b
        SET b.rating = CASE
                WHEN b.reviewCount = 0 THEN 0.0
                ELSE (b.rating * b.reviewCount - :oldGrade + :newGrade) / b.reviewCount
            END
        WHERE b.id = :bookId
        """)
    int changeReviewRating(
        @Param("bookId") UUID bookId,
        @Param("oldGrade") int oldGrade,
        @Param("newGrade") int newGrade
    );
}
