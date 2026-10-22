package com.sprint.deokhugam.dashboard.application.port.in.popularbook;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.dashboard.application.port.out.popularbook.PopularBookRepositoryPort;
import com.sprint.deokhugam.dashboard.domain.PeriodType;
import com.sprint.deokhugam.dashboard.domain.PopularBook;

@ExtendWith(MockitoExtension.class)
class PopularBookRankingServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);

    @Mock
    private PopularBookRepositoryPort popularBookRepositoryPort;

    @InjectMocks
    private PopularBookRankingService popularBookRankingService;

    @Test
    @DisplayName("기간마다 기존 순위를 지우고, 후보를 조회해 저장하는 순서로 집계한다")
    void rankBooksForEveryPeriodInOrder() {
        popularBookRankingService.rankBooks(TODAY);

        InOrder inOrder = inOrder(popularBookRepositoryPort);
        for (PeriodType period : PeriodType.values()) {
            inOrder.verify(popularBookRepositoryPort).deleteByPeriodAndSnapshotDate(period, TODAY);
            inOrder.verify(popularBookRepositoryPort).findCandidates(period, TODAY);
            inOrder.verify(popularBookRepositoryPort).saveAll(List.of());
        }
    }

    @Test
    @DisplayName("후보에 순위를 매겨 점수 높은 순으로 저장한다")
    void rankBooksSavesRankedCandidates() {
        PopularBook lowScore = candidate("A", 1, 5.0);
        PopularBook highScore = candidate("B", 2, 9.0);
        when(popularBookRepositoryPort.findCandidates(PeriodType.DAILY, TODAY))
            .thenReturn(List.of(lowScore, highScore));

        popularBookRankingService.rankBooks(TODAY);

        assertThat(highScore.getRank()).isEqualTo(1L);
        assertThat(lowScore.getRank()).isEqualTo(2L);
        verify(popularBookRepositoryPort).saveAll(List.of(highScore, lowScore));
    }

    private PopularBook candidate(String title, long idNumber, double score) {
        Book book = Book.create(title, null, "저자", "설명", "출판사", LocalDate.of(2020, 1, 1), "isbn-" + title, null);
        ReflectionTestUtils.setField(book, "id", new UUID(0L, idNumber));
        return PopularBook.create(book, PeriodType.DAILY, null, score, 3L, 4.0, TODAY);
    }
}
