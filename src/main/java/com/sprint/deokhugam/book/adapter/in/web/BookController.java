package com.sprint.deokhugam.book.adapter.in.web;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.sprint.deokhugam.book.adapter.in.web.dto.BookUpdateRequest;
import com.sprint.deokhugam.book.application.port.in.BookUseCase;
import com.sprint.deokhugam.book.application.port.in.OcrUseCase;
import com.sprint.deokhugam.book.domain.exception.BookErrorCode;
import com.sprint.deokhugam.book.domain.exception.BookException;
import com.sprint.deokhugam.common.context.user.RequestUserId;
import com.sprint.deokhugam.common.pagination.CursorPageResponse;

@Slf4j
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController implements BookApi {
    private final BookUseCase bookUseCase;
    private final OcrUseCase ocrUseCase;

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
        log.info("도서 정보 조회 API 요청 : isbn : {}", isbn);

        BookInfoDto bookInfoDto = bookUseCase.searchBooksByIsbn(isbn);
        log.info("도서 정보 조회 API 성공 : isbn : {}, title : {}", bookInfoDto.isbn(), bookInfoDto.title());

        return bookInfoDto;
    }

    @DeleteMapping("/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable UUID bookId) {
        log.info("도서 삭제 API 요청 : bookId : {}", bookId);

        bookUseCase.deleteBook(bookId);
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

    @Override
    @PostMapping(value = "/isbn/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public String extractIsbn(@RequestPart("image") MultipartFile image) {
        log.info("ISBN OCR API 요청 : fileName: {}, size: {}", image.getOriginalFilename(), image.getSize());

        String isbn = ocrUseCase.extractIsbn(readBytes(image), image.getContentType());
        log.info("ISBN OCR API 성공 : isbn: {}", isbn);

        return isbn;
    }

    @Override
    @GetMapping("/{bookId}")
    @ResponseStatus(HttpStatus.OK)
    public BookDto getBook(@PathVariable UUID bookId) {
        log.info("도서 상세 조회 API 요청 : bookId : {}", bookId);

        BookDto bookDto = bookUseCase.getBook(bookId);
        log.info("도서 상세 조회 API 성공 : bookId : {}", bookDto.id());

        return bookDto;
    }

    @Override
    @PatchMapping(value = "/{bookId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BookDto updateBook(
        @PathVariable UUID bookId,
        @RequestPart("bookData") @Valid BookUpdateRequest request,
        @RequestPart(
            value = "thumbnailImage",
            required = false
        ) MultipartFile thumbnailImage) {
        log.info("도서 수정 API 요청 : bookId : {}", bookId);

        BookDto bookDto = bookUseCase.updateBook(bookId, request, thumbnailImage);
        log.info("도서 수정 API 성공 : bookId : {}", bookId);

        return bookDto;
    }

    private byte[] readBytes(MultipartFile image) {
        try {
            return image.getBytes();
        } catch (IOException ex) {
            throw new BookException(BookErrorCode.BOOK_OCR_FAILED);
        }
    }
}
