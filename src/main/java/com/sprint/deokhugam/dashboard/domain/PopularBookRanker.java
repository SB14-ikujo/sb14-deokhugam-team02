package com.sprint.deokhugam.dashboard.domain;

import java.util.Comparator;
import java.util.List;

public class PopularBookRanker {

    // Comparator 를 배우시오.. 훌륭하구먼
    private static final Comparator<PopularBook> RANKING_ORDER = Comparator
        .comparingDouble(PopularBook::getScore).reversed()
        .thenComparing(Comparator.comparingLong(PopularBook::getReviewCount).reversed()) // 점수가 같으면 리뷰가 많은 순으로
        .thenComparing(popularBook -> popularBook.getBook().getId()); // 그래도 같으면 id 순으로 정렬한다.

    public List<PopularBook> rank(List<PopularBook> candidates) {
        List<PopularBook> sorted = candidates.stream()
            .sorted(RANKING_ORDER)
            .toList();
        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).assignRank(i + 1L);
        }
        return sorted;
    }
}
