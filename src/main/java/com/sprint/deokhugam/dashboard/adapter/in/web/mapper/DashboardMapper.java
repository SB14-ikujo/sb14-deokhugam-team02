package com.sprint.deokhugam.dashboard.adapter.in.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularBookDto;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularReviewDto;
import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PowerUserDto;
import com.sprint.deokhugam.dashboard.domain.PopularBook;
import com.sprint.deokhugam.dashboard.domain.PopularReview;
import com.sprint.deokhugam.dashboard.domain.PowerUser;

@Mapper(componentModel = "spring")
public interface DashboardMapper {

    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "title", source = "book.title")
    @Mapping(target = "author", source = "book.author")
    @Mapping(target = "thumbnailUrl", source = "book.thumbnailUrl")
    PopularBookDto toDto(PopularBook popularBook);

    @Mapping(target = "userNickname", source = "review.user.nickname")
    @Mapping(target = "userId", source = "review.user.id")
    @Mapping(target = "reviewRating", source = "review.rating")
    @Mapping(target = "reviewId", source = "review.id")
    @Mapping(target = "reviewContent", source = "review.content")
    @Mapping(target = "bookTitle", source = "review.book.title")
    @Mapping(target = "bookThumbnailUrl", source = "review.book.thumbnailUrl")
    @Mapping(target = "bookId", source = "review.book.id")
    PopularReviewDto toDto(PopularReview popularReview);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "nickname", source = "user.nickname")
    PowerUserDto toDto(PowerUser powerUser);

}
