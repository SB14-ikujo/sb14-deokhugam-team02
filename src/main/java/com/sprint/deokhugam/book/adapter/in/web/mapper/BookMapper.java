package com.sprint.deokhugam.book.adapter.in.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.sprint.deokhugam.book.adapter.in.web.dto.BookDto;
import com.sprint.deokhugam.book.adapter.in.web.dto.BookInfoDto;
import com.sprint.deokhugam.book.application.dto.BookSearchResult;
import com.sprint.deokhugam.book.domain.Book;

@Mapper(componentModel = "spring")
public interface BookMapper {

    BookDto toDto(Book book);

    @Mapping(
        target = "author",
        expression = "java(result.authors() == null ? null : String.join(\", \", result.authors()))"
    )
    @Mapping(target = "description", source = "result.contents")
    @Mapping(target = "thumbnailImage", source = "thumbnailImage")
    BookInfoDto toInfoDto(BookSearchResult result, byte[] thumbnailImage);
}
