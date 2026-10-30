package com.sprint.deokhugam.review.application.port.out;

import java.util.List;
import java.util.UUID;

public interface ReviewLikeRepositoryCustom {
    List<UUID> findLikeReviewIds(UUID userId, List<UUID> reviewIds);
}
