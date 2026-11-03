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

    @Mapping(target = "bookId", source = "popularBook.book.id")
    @Mapping(target = "title", source = "popularBook.book.title")
    @Mapping(target = "author", source = "popularBook.book.author")
    @Mapping(target = "thumbnailUrl", source = "bookThumbnailUrl")
    PopularBookDto toDto(PopularBook popularBook, String bookThumbnailUrl);

    @Mapping(target = "userNickname", source = "popularReview.review.user.nickname")
    @Mapping(target = "userId", source = "popularReview.review.user.id")
    @Mapping(target = "reviewRating", source = "popularReview.review.rating")
    @Mapping(target = "reviewId", source = "popularReview.review.id")
    @Mapping(target = "reviewContent", source = "popularReview.review.content")
    @Mapping(target = "bookTitle", source = "popularReview.review.book.title")
    @Mapping(target = "bookId", source = "popularReview.review.book.id")
    @Mapping(target = "bookThumbnailUrl", source = "bookThumbnailUrl")
    PopularReviewDto toDto(PopularReview popularReview, String bookThumbnailUrl);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "nickname", source = "user.nickname")
    PowerUserDto toDto(PowerUser powerUser);

}
