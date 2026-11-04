package com.sprint.deokhugam.dashboard.domain;

import java.util.Comparator;
import java.util.List;

public class PowerUserRanker {

    private static final Comparator<PowerUser> RANKING_ORDER = Comparator
        .comparingDouble(PowerUser::getScore).reversed()
        .thenComparing(Comparator.comparingDouble(PowerUser::getReviewScoreSum).reversed())
        .thenComparing(Comparator.comparingLong(PowerUser::getLikeCount).reversed())
        .thenComparing(Comparator.comparingLong(PowerUser::getCommentCount).reversed())
        .thenComparing(powerUser -> powerUser.getUser().getId());

    public List<PowerUser> rank(List<PowerUser> candidates) {
        List<PowerUser> sorted = candidates.stream()
            .sorted(RANKING_ORDER)
            .toList();

        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).assignRank(i + 1L);
        }
        return sorted;
    }
}
