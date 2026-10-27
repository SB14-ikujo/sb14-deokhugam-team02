package com.sprint.deokhugam.book.adapter.in.web.mapper;

import org.mapstruct.Mapper;

import com.sprint.deokhugam.book.adapter.in.web.dto.BookDto;
import com.sprint.deokhugam.book.domain.Book;

@Mapper(componentModel = "spring")
public interface BookMapper {

    BookDto toDto(Book book);
}
