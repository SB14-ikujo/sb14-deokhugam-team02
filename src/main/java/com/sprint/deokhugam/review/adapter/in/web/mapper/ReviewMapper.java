package com.sprint.deokhugam.review.adapter.in.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewDto;
import com.sprint.deokhugam.review.domain.Review;
import com.sprint.deokhugam.user.domain.User;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @Mapping(target = "id", source = "review.id")
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "bookThumbnailUrl", source = "book.thumbnailUrl")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userNickname", source = "user.nickname")
    @Mapping(target = "rating", source = "review.rating")
    @Mapping(target = "createdAt", source = "review.createdAt")
    @Mapping(target = "updatedAt", source = "review.updatedAt")
    ReviewDto toDto(Review review, User user, Book book, boolean likedByMe);
}
