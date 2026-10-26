package com.sprint.deokhugam.review.adapter.in.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewDto;
import com.sprint.deokhugam.review.domain.Review;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @Mapping(target = "bookId", source = "review.book.id")
    @Mapping(target = "bookTitle", source = "review.book.title")
    @Mapping(target = "bookThumbnailUrl", source = "review.book.thumbnailUrl")
    @Mapping(target = "userId", source = "review.user.id")
    @Mapping(target = "userNickname", source = "review.user.nickname")
    @Mapping(target = "likedByMe", source = "likedByMe")
    ReviewDto toDto(Review review, boolean likedByMe);
}
