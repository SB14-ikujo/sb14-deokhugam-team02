package com.sprint.deokhugam.dashboard.application.port.out;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sprint.deokhugam.dashboard.domain.PopularReview;

public interface PopularReviewRepositoryPort extends JpaRepository<PopularReview, UUID> {
}
