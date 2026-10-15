package com.sprint.deokhugam.dashboard.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.sprint.deokhugam.common.domain.BaseEntity;

@Entity
@Table(name = "popular_reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularReview extends BaseEntity {
}
