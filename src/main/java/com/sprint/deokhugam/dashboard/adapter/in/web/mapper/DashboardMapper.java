package com.sprint.deokhugam.dashboard.adapter.in.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.sprint.deokhugam.dashboard.adapter.in.web.dto.PopularBookDto;
import com.sprint.deokhugam.dashboard.domain.PopularBook;

@Mapper(componentModel = "spring")
public interface DashboardMapper {

    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "title", source = "book.title")
    @Mapping(target = "author", source = "book.author")
    @Mapping(target = "thumbnailUrl", source = "book.thumbnailUrl")
    PopularBookDto toDto(PopularBook popularBook);

}
