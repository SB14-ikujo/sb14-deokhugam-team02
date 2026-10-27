package com.sprint.deokhugam.book.adapter.in.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.book.adapter.in.web.dto.BookCreateRequest;
import com.sprint.deokhugam.book.adapter.in.web.dto.BookDto;
import com.sprint.deokhugam.book.application.port.in.BookUseCase;
import com.sprint.deokhugam.common.context.user.RequestUserId;

@Slf4j
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController implements BookApi {
    private final BookUseCase bookUseCase;

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDto createBook(
        @RequestUserId UUID userId,
        @RequestBody @Valid BookCreateRequest request
    ) {
        log.info("도서 등록 API 요청 : userId: {}", userId);

        BookDto bookDto = bookUseCase.createBook(userId, request);
        log.info("도서 등록 API 성공 : bookId: {}", bookDto.id());

        return bookDto;
    }
}
