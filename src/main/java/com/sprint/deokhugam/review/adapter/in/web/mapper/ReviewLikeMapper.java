package com.sprint.deokhugam.review.adapter.in.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewLikeDto;
import com.sprint.deokhugam.review.domain.ReviewLike;

@Mapper(componentModel = "spring")
public interface ReviewLikeMapper {
    @Mapping(target = "reviewId", source = "review.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "liked", source = "liked")
    ReviewLikeDto toDto(ReviewLike reviewLike, boolean liked);

}

