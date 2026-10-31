package com.sprint.deokhugam.book.adapter.in.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.book.adapter.in.web.dto.BookCreateRequest;
import com.sprint.deokhugam.book.adapter.in.web.dto.BookDto;
import com.sprint.deokhugam.book.adapter.in.web.dto.BookInfoDto;
import com.sprint.deokhugam.book.adapter.in.web.dto.BookSearchRequest;
import com.sprint.deokhugam.book.application.port.in.BookUseCase;
import com.sprint.deokhugam.common.context.user.RequestUserId;
import com.sprint.deokhugam.common.pagination.CursorPageResponse;

@Slf4j
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController implements BookApi {
    private final BookUseCase bookUseCase;

    @Override
    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public BookDto createBook(
        @RequestUserId UUID userId,
        @RequestPart("bookData") @Valid BookCreateRequest request,
        @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
    ) {
        log.info("도서 등록 API 요청 : userId: {}", userId);

        BookDto bookDto = bookUseCase.createBook(userId, request, thumbnailImage);
        log.info("도서 등록 API 성공 : bookId: {}", bookDto.id());

        return bookDto;
    }

    @Override
    @GetMapping("/info")
    @ResponseStatus(HttpStatus.OK)
    public BookInfoDto searchBooksByIsbn(@RequestParam String isbn) {
        log.info("도서 정보 조회 요청 : isbn : {}", isbn);

        BookInfoDto bookInfoDto = bookUseCase.searchBooksByIsbn(isbn);
        log.info("도서 정보 조회 성공 : isbn : {}, title : {}", bookInfoDto.isbn(), bookInfoDto.title());

        return bookInfoDto;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public CursorPageResponse<BookDto> getBooks(
        @Valid BookSearchRequest request
    ) {
        return bookUseCase.getBooks(
            request.keyword(), request.orderBy(), request.direction(),
            request.cursor(), request.after(), request.limit()
        );
    }
}
