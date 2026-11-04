package com.sprint.deokhugam.dashboard.domain;

import java.util.Comparator;
import java.util.List;

public class PopularReviewRanker {

    private static final Comparator<PopularReview> RANKING_ORDER = Comparator
        .comparingDouble(PopularReview::getScore).reversed()
        .thenComparing(Comparator.comparingLong(PopularReview::getCommentCount).reversed())
        .thenComparing(popularReview -> popularReview.getReview().getId());

    public List<PopularReview> rank(List<PopularReview> candidates) {
        List<PopularReview> sorted = candidates.stream()
            .sorted(RANKING_ORDER)
            .toList();

        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).assignRank(i + 1L);
        }
        return sorted;
    }

}
