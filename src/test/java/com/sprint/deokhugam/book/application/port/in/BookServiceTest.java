package com.sprint.deokhugam.book.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.sprint.deokhugam.book.adapter.in.web.dto.BookCreateRequest;
import com.sprint.deokhugam.book.adapter.in.web.dto.BookDto;
import com.sprint.deokhugam.book.adapter.in.web.dto.BookInfoDto;
import com.sprint.deokhugam.book.adapter.in.web.mapper.BookMapper;
import com.sprint.deokhugam.book.application.dto.BookSearchResult;
import com.sprint.deokhugam.book.application.dto.DownloadedThumbnail;
import com.sprint.deokhugam.book.application.port.out.BookImageStorage;
import com.sprint.deokhugam.book.application.port.out.BookRepositoryPort;
import com.sprint.deokhugam.book.application.port.out.BookSearchPort;
import com.sprint.deokhugam.book.application.port.out.BookThumbnailDownloadPort;
import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.book.domain.exception.BookErrorCode;
import com.sprint.deokhugam.book.domain.exception.BookException;
import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.user.application.port.out.UserRepositoryPort;
import com.sprint.deokhugam.user.domain.User;
import com.sprint.deokhugam.user.domain.exception.UserException;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    private static final Instant CREATED_AT = Instant.parse("2026-10-01T00:00:00Z");

    @Mock
    private BookRepositoryPort bookRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private BookSearchPort bookSearchPort;

    @Mock
    private BookImageStorage bookImageStorage;

    @Mock
    private BookThumbnailDownloadPort bookThumbnailDownloadPort;

    @InjectMocks
    private BookService bookService;

    private UUID userId;
    private BookCreateRequest request;
    private User user;
    private BookDto expectedDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        request = new BookCreateRequest(
            "원피스 99: 밀짚모자 루피 (밀짚모자 루피)",
            "오다 에이치로",
            "간부들의 맹추격을 동료들이 저지하고, 옥상을 향한 루피는 마침내 카이도에게?! "
                + "큰 무대에 모인 배우들의 격전이 지금, 시작되려고 한다... 오니가시마 결전 최대 히트 업!! "
                + "'원피스'를 둘러싼 해양 모험 로망!!",
            "대원씨아이",
            LocalDate.of(2023, 8, 16),
            "9791168945456"
        );

        user = org.mockito.Mockito.mock(User.class);

        expectedDto = new BookDto(
            UUID.randomUUID(),
            request.title(),
            request.author(),
            request.description(),
            request.publisher(),
            request.publishedDate(),
            request.isbn(),
            null,
            0,
            0.0,
            Instant.now(),
            Instant.now()
        );

        // 목 매퍼는 기본적으로 null을 반환하므로, 목록 조회 테스트에서 실제 매핑처럼 동작하도록 스텁한다
        lenient().when(bookMapper.toDto(any(Book.class)))
            .thenAnswer(invocation -> toDto(invocation.getArgument(0)));
    }

    private BookDto toDto(Book book) {
        return new BookDto(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getDescription(),
            book.getPublisher(),
            book.getPublishedDate(),
            book.getIsbn(),
            book.getThumbnailUrl(),
            book.getReviewCount(),
            book.getRating(),
            book.getCreatedAt(),
            book.getUpdatedAt()
        );
    }

    @Test
    @DisplayName("정상적인 요청이면 도서를 등록하고 DTO를 반환한다.")
    void createBookSuccess() {
        // given
        when(bookRepositoryPort.existsByIsbn(request.isbn()))
            .thenReturn(false);

        when(userRepositoryPort.findByIdAndIsDeletedFalse(userId))
            .thenReturn(Optional.of(user));

        when(bookSearchPort.searchByIsbn(request.isbn()))
            .thenReturn(List.of());

        when(bookRepositoryPort.saveAndFlush(any(Book.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(bookMapper.toDto(any(Book.class)))
            .thenReturn(expectedDto);

        // when
        BookDto result = bookService.createBook(userId, request, null);

        // then
        assertThat(result).isEqualTo(expectedDto);

        verify(bookRepositoryPort).existsByIsbn(request.isbn());
        verify(userRepositoryPort).findByIdAndIsDeletedFalse(userId);

        // saveAndFlush()에 전달된 Book 객체 확인
        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);

        verify(bookRepositoryPort).saveAndFlush(bookCaptor.capture());

        Book savedBook = bookCaptor.getValue();

        assertThat(savedBook.getTitle()).isEqualTo(request.title());
        assertThat(savedBook.getAuthor()).isEqualTo(request.author());
        assertThat(savedBook.getIsbn()).isEqualTo(request.isbn());
        assertThat(savedBook.getUser()).isSameAs(user);

        verify(bookMapper).toDto(savedBook);
    }

    @Test
    @DisplayName("ISBN 형식이 잘못되면 예외가 발생하고 저장하지 않는다.")
    void createBookWithInvalidIsbn() {
        // given
        BookCreateRequest invalidRequest = new BookCreateRequest(
            "원피스 99: 밀짚모자 루피 (밀짚모자 루피)",
            "오다 에이치로",
            "간부들의 맹추격을 동료들이 저지하고, 옥상을 향한 루피는 마침내 카이도에게?! "
                + "큰 무대에 모인 배우들의 격전이 지금, 시작되려고 한다... 오니가시마 결전 최대 히트 업!! "
                + "'원피스'를 둘러싼 해양 모험 로망!!",
            "대원씨아이",
            LocalDate.of(2023, 8, 16),
            "12345"
        );

        // when & then
        assertThatThrownBy(() -> bookService.createBook(userId, invalidRequest, null))
            .isInstanceOf(BookException.class)
            .hasMessage(BookErrorCode.BOOK_INVALID_ISBN.getMessage());

        verify(bookRepositoryPort, never()).existsByIsbn(any(String.class));
        verify(bookRepositoryPort, never()).saveAndFlush(any(Book.class));
    }

    @Test
    @DisplayName("이미 등록된 ISBN이면 예외가 발생하고 저장하지 않는다.")
    void createBookWithDuplicateIsbn() {
        // given
        when(bookRepositoryPort.existsByIsbn(request.isbn()))
            .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> bookService.createBook(userId, request, null))
            .isInstanceOf(BookException.class)
            .hasMessage(BookErrorCode.BOOK_ISBN_DUPLICATE.getMessage());

        verify(bookRepositoryPort).existsByIsbn(request.isbn());
        verify(userRepositoryPort, never()).findByIdAndIsDeletedFalse(any(UUID.class));
        verify(bookRepositoryPort, never()).saveAndFlush(any(Book.class));
    }

    @Test
    @DisplayName("사용자를 찾을 수 없으면 예외가 발생하고 저장하지 않는다.")
    void createBookWithNonexistenUser() {
        // given
        when(bookRepositoryPort.existsByIsbn(request.isbn()))
            .thenReturn(false);

        when(userRepositoryPort.findByIdAndIsDeletedFalse(userId))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookService.createBook(userId, request, null))
            .isInstanceOf(UserException.class);

        verify(bookRepositoryPort, never()).saveAndFlush(any(Book.class));
    }

    @Test
    @DisplayName("하이픈이 포함된 ISBN은 정규화된 값으로 중복 확인하고 저장한다.")
    void createBookWithHyphenatedIsbn() {
        // given
        BookCreateRequest hyphenatedRequest = new BookCreateRequest(
            "원피스 99: 밀짚모자 루피 (밀짚모자 루피)",
            "오다 에이치로",
            "간부들의 맹추격을 동료들이 저지하고, 옥상을 향한 루피는 마침내 카이도에게?! "
                + "큰 무대에 모인 배우들의 격전이 지금, 시작되려고 한다... 오니가시마 결전 최대 히트 업!! "
                + "'원피스'를 둘러싼 해양 모험 로망!!",
            "대원씨아이",
            LocalDate.of(2023, 8, 16),
            "979-116894-545-6"
        );

        String normalizedIsbn = "9791168945456";

        when(bookRepositoryPort.existsByIsbn(normalizedIsbn))
            .thenReturn(false);

        when(userRepositoryPort.findByIdAndIsDeletedFalse(userId))
            .thenReturn(Optional.of(user));

        when(bookRepositoryPort.saveAndFlush(any(Book.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(bookMapper.toDto(any(Book.class)))
            .thenReturn(expectedDto);

        // when
        bookService.createBook(userId, hyphenatedRequest, null);

        // then
        verify(bookRepositoryPort).existsByIsbn(normalizedIsbn);

        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);

        verify(bookRepositoryPort).saveAndFlush(bookCaptor.capture());

        assertThat(bookCaptor.getValue().getIsbn())
            .isEqualTo(normalizedIsbn);
    }

    @Test
    @DisplayName("ISBN으로 도서를 검색하고 검색 결과를 반환한다.")
    void searchBooksByIsbnReturnsSearchResults() {
        // given
        String isbn = "9791168945456";

        BookSearchResult searchResult = new BookSearchResult(
            "원피스 99: 밀짚모자 루피 (밀짚모자 루피)",
            "간부들의 맹추격을 동료들이 저지하고, 옥상을 향한 루피는 마침내 카이도에게?! "
                + "큰 무대에 모인 배우들의 격전이 지금, 시작되려고 한다... 오니가시마 결전 최대 히트 업!! "
                + "'원피스'를 둘러싼 해양 모험 로망!!",
            "https://example.com/book",
            isbn,
            LocalDate.of(2023, 8, 16),
            List.of("오다 에이치로"),
            "대원씨아이",
            List.of("김지현"),
            15000,
            10000,
            null,
            "정상판매"
        );

        BookInfoDto expected = new BookInfoDto(
            "원피스 99: 밀짚모자 루피 (밀짚모자 루피)",
            "오다 에이치로",
            "간부들의 맹추격을 동료들이 저지하고, 옥상을 향한 루피는 마침내 카이도에게?! "
                + "큰 무대에 모인 배우들의 격전이 지금, 시작되려고 한다... 오니가시마 결전 최대 히트 업!! "
                + "'원피스'를 둘러싼 해양 모험 로망!!",
            "대원씨아이",
            LocalDate.of(2023, 8, 16),
            isbn,
            null
        );

        given(bookSearchPort.searchByIsbn(isbn))
            .willReturn(List.of(searchResult));

        given(bookMapper.toInfoDto(searchResult, null))
            .willReturn(expected);

        // when
        BookInfoDto result = bookService.searchBooksByIsbn(isbn);

        // then
        assertThat(result).isEqualTo(expected);
        assertThat(result.title()).isEqualTo(searchResult.title());
        assertThat(result.author()).isEqualTo("오다 에이치로");
        assertThat(result.isbn()).isEqualTo(isbn);
        assertThat(result.thumbnailImage()).isNull();

        then(bookSearchPort).should().searchByIsbn(isbn);
        then(bookMapper).should().toInfoDto(searchResult, null);
        then(bookThumbnailDownloadPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("ISBN 검색 결과가 없으면 도서 정보 없음 예외가 발생한다.")
    void searchBooksByIsbnThrowsExceptionWhenNoResult() {
        // given
        String isbn = "9791168945456";

        given(bookSearchPort.searchByIsbn(isbn))
            .willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> bookService.searchBooksByIsbn(isbn))
            .isInstanceOf(BookException.class)
            .hasMessage(BookErrorCode.BOOK_INFO_NOT_FOUND.getMessage());

        then(bookSearchPort).should().searchByIsbn(isbn);
        then(bookThumbnailDownloadPort).shouldHaveNoInteractions();
        then(bookMapper).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("직접 첨부한 썸네일 이미지를 저장하고 URL을 반환한다.")
    void createBookWithUploadedThumbnail() throws IOException {
        // given
        MockMultipartFile thumbnailImage = new MockMultipartFile(
            "thumbnailImage",
            "thumbnail.png",
            "image/png",
            createValidPngBytes()
        );

        when(bookRepositoryPort.existsByIsbn(request.isbn()))
            .thenReturn(false);

        when(userRepositoryPort.findByIdAndIsDeletedFalse(userId))
            .thenReturn(Optional.of(user));

        when(bookImageStorage.generateImageKey("thumbnail.png"))
            .thenReturn("generated_thumbnail.png");

        when(bookImageStorage.upload(
            eq("generated_thumbnail.png"),
            any(byte[].class),
            eq("image/png")
        )).thenReturn("generated_thumbnail.png");

        when(bookImageStorage.getImageUrl(
            "generated_thumbnail.png",
            "image/png"
        )).thenReturn("/api/images/generated_thumbnail.png");

        when(bookRepositoryPort.saveAndFlush(any(Book.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(bookMapper.toDto(any(Book.class)))
            .thenReturn(expectedDto);

        // when
        BookDto result = bookService.createBook(
            userId,
            request,
            thumbnailImage
        );

        // then
        assertThat(result.thumbnailUrl()).isEqualTo("/api/images/generated_thumbnail.png");

        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);

        verify(bookRepositoryPort).saveAndFlush(bookCaptor.capture());

        Book savedBook = bookCaptor.getValue();

        assertThat(savedBook.getThumbnailUrl()).isEqualTo("generated_thumbnail.png");

        verify(bookImageStorage).upload(
            eq("generated_thumbnail.png"),
            any(byte[].class),
            eq("image/png")
        );

        verify(bookSearchPort, never()).searchByIsbn(anyString());
    }

    @Test
    @DisplayName("첨부 이미지가 없으면 카카오 썸네일을 다운로드해 저장한다.")
    void createBookWithKakaoThumbnail() throws IOException {
        // given
        String thumbnailUrl =
            "https://search1.kakaocdn.net/argon/130x130/thumbnail.png";

        byte[] imageBytes = createValidPngBytes();

        BookSearchResult searchResult = new BookSearchResult(
            request.title(),
            request.description(),
            "https://example.com/book",
            request.isbn(),
            request.publishedDate(),
            List.of(request.author()),
            request.publisher(),
            List.of(),
            15000,
            10000,
            thumbnailUrl,
            "정상판매"
        );

        DownloadedThumbnail downloadedThumbnail = new DownloadedThumbnail(
            imageBytes,
            "image/png",
            "kakao-thumbnail.png"
        );

        when(bookRepositoryPort.existsByIsbn(request.isbn()))
            .thenReturn(false);

        when(userRepositoryPort.findByIdAndIsDeletedFalse(userId))
            .thenReturn(Optional.of(user));

        when(bookSearchPort.searchByIsbn(request.isbn()))
            .thenReturn(List.of(searchResult));

        when(bookThumbnailDownloadPort.download(thumbnailUrl))
            .thenReturn(Optional.of(downloadedThumbnail));

        when(bookImageStorage.generateImageKey("kakao-thumbnail.png"))
            .thenReturn("generated_kakao-thumbnail.png");

        when(bookImageStorage.upload(
            eq("generated_kakao-thumbnail.png"),
            any(byte[].class),
            eq("image/png")
        )).thenReturn("generated_kakao-thumbnail.png");

        when(bookImageStorage.getImageUrl(
            "generated_kakao-thumbnail.png",
            "image/png"
        )).thenReturn("/api/images/generated_kakao-thumbnail.png");

        when(bookRepositoryPort.saveAndFlush(any(Book.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(bookMapper.toDto(any(Book.class)))
            .thenReturn(expectedDto);

        // when
        BookDto result = bookService.createBook(userId, request, null);

        // then
        assertThat(result.thumbnailUrl())
            .isEqualTo("/api/images/generated_kakao-thumbnail.png");

        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);

        verify(bookRepositoryPort).saveAndFlush(bookCaptor.capture());

        Book savedBook = bookCaptor.getValue();

        assertThat(savedBook.getThumbnailUrl()).isEqualTo("generated_kakao-thumbnail.png");

        verify(bookSearchPort).searchByIsbn(request.isbn());
        verify(bookThumbnailDownloadPort).download(thumbnailUrl);

        verify(bookImageStorage).upload(
            eq("generated_kakao-thumbnail.png"),
            any(byte[].class),
            eq("image/png")
        );
    }

    @Test
    @DisplayName("DB 저장에 실패하면 업로드한 썸네일을 삭제한다.")
    void createBookDeletesThumbnailWhenSaveFails() throws IOException {
        // given
        MockMultipartFile thumbnailImage = new MockMultipartFile(
            "thumbnailImage",
            "thumbnail.png",
            "image/png",
            createValidPngBytes()
        );

        when(bookRepositoryPort.existsByIsbn(request.isbn()))
            .thenReturn(false);

        when(userRepositoryPort.findByIdAndIsDeletedFalse(userId))
            .thenReturn(Optional.of(user));

        when(bookImageStorage.generateImageKey("thumbnail.png"))
            .thenReturn("generated_thumbnail.png");

        when(bookImageStorage.upload(
            eq("generated_thumbnail.png"),
            any(byte[].class),
            eq("image/png")
        )).thenReturn("generated_thumbnail.png");

        when(bookRepositoryPort.saveAndFlush(any(Book.class)))
            .thenThrow(new RuntimeException("DB 저장 실패"));

        // when & then
        assertThatThrownBy(
            () -> bookService.createBook(userId, request, thumbnailImage)
        ).isInstanceOf(RuntimeException.class)
            .hasMessage("DB 저장 실패");

        verify(bookImageStorage).delete("generated_thumbnail.png");
    }

    @Test
    @DisplayName("카카오 검색 결과가 없으면 썸네일 없이 도서를 등록한다.")
    void createBookWithoutKakaoThumbnail() {
        // given
        when(bookRepositoryPort.existsByIsbn(request.isbn()))
            .thenReturn(false);

        when(userRepositoryPort.findByIdAndIsDeletedFalse(userId))
            .thenReturn(Optional.of(user));

        when(bookSearchPort.searchByIsbn(request.isbn()))
            .thenReturn(List.of());

        when(bookRepositoryPort.saveAndFlush(any(Book.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(bookMapper.toDto(any(Book.class)))
            .thenReturn(expectedDto);

        // when
        BookDto result = bookService.createBook(userId, request, null);

        // then
        assertThat(result.thumbnailUrl()).isNull();

        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);

        verify(bookRepositoryPort).saveAndFlush(bookCaptor.capture());

        assertThat(bookCaptor.getValue().getThumbnailUrl()).isNull();

        verify(bookImageStorage, never()).upload(anyString(), any(byte[].class), anyString());
    }

    @Test
    @DisplayName("카카오 썸네일 다운로드에 실패하면 이미지 없이 도서를 등록한다.")
    void createBookWithoutThumbnailWhenDownloadFails() {
        // given
        String thumbnailUrl = "https://search1.kakaocdn.net/argon/130x130/thumbnail.png";

        BookSearchResult searchResult = new BookSearchResult(
            request.title(),
            request.description(),
            "https://example.com/book",
            request.isbn(),
            request.publishedDate(),
            List.of(request.author()),
            request.publisher(),
            List.of(),
            15000,
            10000,
            thumbnailUrl,
            "정상판매"
        );

        when(bookRepositoryPort.existsByIsbn(request.isbn()))
            .thenReturn(false);

        when(userRepositoryPort.findByIdAndIsDeletedFalse(userId))
            .thenReturn(Optional.of(user));

        when(bookSearchPort.searchByIsbn(request.isbn()))
            .thenReturn(List.of(searchResult));

        when(bookThumbnailDownloadPort.download(thumbnailUrl))
            .thenReturn(Optional.empty());

        when(bookRepositoryPort.saveAndFlush(any(Book.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(bookMapper.toDto(any(Book.class)))
            .thenReturn(expectedDto);

        // when
        BookDto result = bookService.createBook(userId, request, null);

        // then
        assertThat(result.thumbnailUrl()).isNull();

        verify(bookThumbnailDownloadPort).download(thumbnailUrl);

        verify(bookImageStorage, never()).upload(anyString(), any(byte[].class), anyString());

        verify(bookRepositoryPort).saveAndFlush(any(Book.class));
    }

    @Test
    @DisplayName("파일 확장자와 MIME 타입이 다르면 MIME 타입에 맞게 확장자를 보정한다.")
    void createBookWithMismatchedExtension() throws Exception {
        // given
        MockMultipartFile thumbnailImage = new MockMultipartFile(
            "thumbnailImage",
            "thumbnail.jpg",
            "image/png",
            createValidPngBytes()
        );

        given(bookRepositoryPort.existsByIsbn(request.isbn()))
            .willReturn(false);

        given(userRepositoryPort.findByIdAndIsDeletedFalse(userId))
            .willReturn(Optional.of(user));

        given(bookImageStorage.generateImageKey("thumbnail.png"))
            .willReturn("generated_thumbnail.png");

        given(bookImageStorage.upload(
            eq("generated_thumbnail.png"),
            any(byte[].class),
            eq("image/png")
        )).willReturn("generated_thumbnail.png");

        given(bookImageStorage.getImageUrl(
            "generated_thumbnail.png",
            "image/png"
        )).willReturn("/api/images/generated_thumbnail.png");

        given(bookRepositoryPort.saveAndFlush(any(Book.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        given(bookMapper.toDto(any(Book.class)))
            .willReturn(expectedDto);

        // when
        bookService.createBook(userId, request, thumbnailImage);

        // then
        verify(bookImageStorage).generateImageKey("thumbnail.png");

        verify(bookImageStorage).upload(
            eq("generated_thumbnail.png"),
            any(byte[].class),
            eq("image/png")
        );
    }

    @Test
    @DisplayName("파일명에 확장자가 없으면 MIME 타입에 맞게 확장자를 추가한다.")
    void createBookWithoutFileExtension() throws Exception {
        // given
        MockMultipartFile thumbnailImage = new MockMultipartFile(
            "thumbnailImage",
            "thumbnail",
            "image/png",
            createValidPngBytes()
        );

        given(bookRepositoryPort.existsByIsbn(request.isbn()))
            .willReturn(false);

        given(userRepositoryPort.findByIdAndIsDeletedFalse(userId))
            .willReturn(Optional.of(user));

        given(bookImageStorage.generateImageKey("thumbnail.png"))
            .willReturn("generated_thumbnail.png");

        given(bookImageStorage.upload(
            eq("generated_thumbnail.png"),
            any(byte[].class),
            eq("image/png")
        )).willReturn("generated_thumbnail.png");

        given(bookImageStorage.getImageUrl(
            "generated_thumbnail.png",
            "image/png"
        )).willReturn("/api/images/generated_thumbnail.png");

        given(bookRepositoryPort.saveAndFlush(any(Book.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        given(bookMapper.toDto(any(Book.class)))
            .willReturn(expectedDto);

        // when
        bookService.createBook(userId, request, thumbnailImage);

        // then
        verify(bookImageStorage).generateImageKey("thumbnail.png");
    }

    @Test
    @DisplayName("실제 이미지 형식과 Content-Type이 다르면 업로드에 실패한다")
    void createBookWithMismatchedContentType() throws IOException {
        given(userRepositoryPort.findByIdAndIsDeletedFalse(userId))
            .willReturn(Optional.of(user));

        MockMultipartFile thumbnailImage = new MockMultipartFile(
            "thumbnailImage",
            "thumbnail.jpg",
            "image/jpeg",
            createValidPngBytes()
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> bookService.createBook(userId, request, thumbnailImage)
        );

        then(bookImageStorage).should(never())
            .upload(anyString(), any(byte[].class), anyString());
    }

    @Test
    @DisplayName("지원하지 않는 파일 형식이면 업로드에 실패한다")
    void createBookWithUnsupportedImageFormat() {
        given(userRepositoryPort.findByIdAndIsDeletedFalse(userId))
            .willReturn(Optional.of(user));

        MockMultipartFile thumbnailImage = new MockMultipartFile(
            "thumbnailImage",
            "thumbnail.png",
            "image/png",
            "This is not an image".getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> bookService.createBook(userId, request, thumbnailImage)
        );

        then(bookImageStorage).should(never())
            .upload(anyString(), any(byte[].class), anyString());
    }

    @Test
    @DisplayName("이미지 크기가 5MB를 초과하면 업로드에 실패한다")
    void createBookWithOversizedThumbnail() {
        // given
        given(userRepositoryPort.findByIdAndIsDeletedFalse(userId))
            .willReturn(Optional.of(user));

        byte[] oversizedImage = new byte[5 * 1024 * 1024 + 1];

        MockMultipartFile thumbnailImage = new MockMultipartFile(
            "thumbnailImage",
            "thumbnail.png",
            "image/png",
            oversizedImage
        );

        // when & then
        assertThatThrownBy(() -> bookService.createBook(userId, request, thumbnailImage))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이미지 파일 크기는 5MB를 초과할 수 없습니다.");

        then(bookImageStorage).should(never())
            .upload(anyString(), any(byte[].class), anyString());

        then(bookRepositoryPort).should(never())
            .saveAndFlush(any(Book.class));
    }

    @Test
    @DisplayName("ISBN 검색 시 썸네일을 다운로드하고 도서 정보와 함께 반환한다.")
    void searchBooksByIsbnWithThumbnail() throws IOException {
        // given
        String isbn = "9791168945456";
        String thumbnailUrl = "https://example.com/book.jpg";

        byte[] imageBytes = createValidPngBytes();

        BookSearchResult searchResult = new BookSearchResult(
            "원피스 99",
            "도서 소개",
            "https://example.com/book",
            isbn,
            LocalDate.of(2023, 8, 16),
            List.of("오다 에이치로"),
            "대원씨아이",
            List.of(),
            15000,
            10000,
            thumbnailUrl,
            "정상판매"
        );

        DownloadedThumbnail downloadedThumbnail = new DownloadedThumbnail(
            imageBytes,
            "image/png",
            "thumbnail.png"
        );

        BookInfoDto expected = new BookInfoDto(
            "원피스 99",
            "오다 에이치로",
            "도서 소개",
            "대원씨아이",
            LocalDate.of(2023, 8, 16),
            isbn,
            imageBytes
        );

        given(bookSearchPort.searchByIsbn(isbn))
            .willReturn(List.of(searchResult));

        given(bookThumbnailDownloadPort.download(thumbnailUrl))
            .willReturn(Optional.of(downloadedThumbnail));

        given(bookMapper.toInfoDto(searchResult, imageBytes))
            .willReturn(expected);

        // when
        BookInfoDto result = bookService.searchBooksByIsbn(isbn);

        // then
        assertThat(result).isEqualTo(expected);
        assertThat(result.thumbnailImage()).isEqualTo(imageBytes);

        then(bookSearchPort).should().searchByIsbn(isbn);
        then(bookThumbnailDownloadPort).should().download(thumbnailUrl);
        then(bookMapper).should().toInfoDto(searchResult, imageBytes);
    }

    @Test
    @DisplayName("썸네일 다운로드 실패 시에도 도서 정보를 반환한다.")
    void searchBooksByIsbnWithoutThumbnailWhenDownloadFails() {
        // given
        String isbn = "9791168945456";
        String thumbnailUrl = "https://example.com/book.jpg";

        BookSearchResult searchResult = new BookSearchResult(
            "원피스 99",
            "도서 소개",
            "https://example.com/book",
            isbn,
            LocalDate.of(2023, 8, 16),
            List.of("오다 에이치로"),
            "대원씨아이",
            List.of(),
            15000,
            10000,
            thumbnailUrl,
            "정상판매"
        );

        BookInfoDto expected = new BookInfoDto(
            "원피스 99",
            "오다 에이치로",
            "도서 소개",
            "대원씨아이",
            LocalDate.of(2023, 8, 16),
            isbn,
            null
        );

        given(bookSearchPort.searchByIsbn(isbn))
            .willReturn(List.of(searchResult));

        given(bookThumbnailDownloadPort.download(thumbnailUrl))
            .willThrow(new RuntimeException("썸네일 다운로드 실패"));

        given(bookMapper.toInfoDto(searchResult, null))
            .willReturn(expected);

        // when
        BookInfoDto result = bookService.searchBooksByIsbn(isbn);

        // then
        assertThat(result).isEqualTo(expected);
        assertThat(result.thumbnailImage()).isNull();

        then(bookThumbnailDownloadPort).should().download(thumbnailUrl);
        then(bookMapper).should().toInfoDto(searchResult, null);
    }

    @Test
    @DisplayName("잘못된 ISBN으로 검색하면 예외가 발생하고 카카오 API를 호출하지 않는다.")
    void searchBooksByIsbnWithInvalidIsbn() {
        // given
        String invalidIsbn = "12345";

        // when & then
        assertThatThrownBy(() -> bookService.searchBooksByIsbn(invalidIsbn))
            .isInstanceOf(BookException.class)
            .hasMessage(BookErrorCode.BOOK_INVALID_ISBN.getMessage());

        then(bookSearchPort).shouldHaveNoInteractions();
        then(bookThumbnailDownloadPort).shouldHaveNoInteractions();
        then(bookMapper).shouldHaveNoInteractions();
    }

    private byte[] createValidPngBytes() throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", outputStream);
            return outputStream.toByteArray();
        }
    }

    private Book book(String title, LocalDate publishedDate, double rating, int reviewCount, Instant createdAt) {
        Book book = Book.create(title, mock(User.class), "저자", "설명", "출판사",
            publishedDate, "isbn-" + title, null);
        ReflectionTestUtils.setField(book, "rating", rating);
        ReflectionTestUtils.setField(book, "reviewCount", reviewCount);
        ReflectionTestUtils.setField(book, "createdAt", createdAt);
        return book;
    }

    private Book book(String title) {
        return book(title, LocalDate.of(2020, 1, 1), 4.0, 1, CREATED_AT);
    }

    @Test
    @DisplayName("조회 결과가 limit 이하이면 다음 페이지가 없고 커서는 null이다")
    void getBooks_noNext() {
        given(bookRepositoryPort.countByKeyword(null)).willReturn(2L);
        given(bookRepositoryPort.findAllByCursor(null, "title", Sort.Direction.ASC, null, null, 5))
            .willReturn(List.of(book("A"), book("B")));

        CursorPageResponse<BookDto> response =
            bookService.getBooks(null, "title", Sort.Direction.ASC, null, null, 5);

        assertThat(response.content()).extracting(BookDto::title).containsExactly("A", "B");
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
        assertThat(response.nextAfter()).isNull();
        assertThat(response.totalElements()).isEqualTo(2L);
    }

    @Test
    @DisplayName("limit보다 1건 더 조회되면 limit건만 반환하고 다음 페이지가 있다")
    void getBooks_hasNext() {
        Instant secondCreatedAt = CREATED_AT.plusSeconds(60);
        given(bookRepositoryPort.countByKeyword(null)).willReturn(3L);
        given(bookRepositoryPort.findAllByCursor(null, "title", Sort.Direction.ASC, null, null, 2))
            .willReturn(List.of(
                book("A"),
                book("B", LocalDate.of(2020, 1, 1), 4.0, 1, secondCreatedAt),
                book("C")));

        CursorPageResponse<BookDto> response =
            bookService.getBooks(null, "title", Sort.Direction.ASC, null, null, 2);

        assertThat(response.content()).extracting(BookDto::title).containsExactly("A", "B");
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isEqualTo("B");        // 마지막 항목 기준
        assertThat(response.nextAfter()).isEqualTo(secondCreatedAt);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(3L);
    }

    @ParameterizedTest(name = "orderBy={0} → nextCursor={1}")
    @CsvSource({
        "title,T",
        "publishedDate,2020-01-02",
        "rating,4.5",
        "reviewCount,7"
    })
    @DisplayName("정렬 기준에 맞는 값이 다음 커서로 만들어진다")
    void getBooks_cursorByOrderBy(String orderBy, String expectedCursor) {
        Book first = book("T", LocalDate.of(2020, 1, 2), 4.5, 7, CREATED_AT);
        Book extra = book("U", LocalDate.of(2020, 1, 3), 1.0, 1, CREATED_AT);
        given(bookRepositoryPort.countByKeyword(null)).willReturn(2L);
        given(bookRepositoryPort.findAllByCursor(null, orderBy, Sort.Direction.DESC, null, null, 1))
            .willReturn(List.of(first, extra));

        CursorPageResponse<BookDto> response =
            bookService.getBooks(null, orderBy, Sort.Direction.DESC, null, null, 1);

        assertThat(response.nextCursor()).isEqualTo(expectedCursor);
        assertThat(response.nextAfter()).isEqualTo(CREATED_AT);
    }

    @Test
    @DisplayName("알 수 없는 정렬 기준이면 제목 기준 커서를 사용한다")
    void getBooks_unknownOrderByUsesTitleCursor() {
        given(bookRepositoryPort.countByKeyword(null)).willReturn(2L);
        given(bookRepositoryPort.findAllByCursor(null, "unknown", Sort.Direction.ASC, null, null, 1))
            .willReturn(List.of(book("A"), book("B")));

        CursorPageResponse<BookDto> response =
            bookService.getBooks(null, "unknown", Sort.Direction.ASC, null, null, 1);

        assertThat(response.nextCursor()).isEqualTo("A");
    }

    @Test
    @DisplayName("조회 조건을 그대로 리포지토리에 전달한다")
    void getBooks_passesConditions() {
        Instant after = Instant.parse("2026-10-02T00:00:00Z");
        given(bookRepositoryPort.countByKeyword("자바")).willReturn(0L);
        given(bookRepositoryPort.findAllByCursor("자바", "rating", Sort.Direction.DESC, "4.5", after, 20))
            .willReturn(List.of());

        bookService.getBooks("자바", "rating", Sort.Direction.DESC, "4.5", after, 20);

        then(bookRepositoryPort).should().findAllByCursor("자바", "rating", Sort.Direction.DESC, "4.5", after, 20);
        then(bookRepositoryPort).should().countByKeyword("자바");   // 총 개수는 커서와 무관하게 키워드로만 센다
    }

    @Test
    @DisplayName("조회 결과가 없으면 빈 목록과 총 개수 0을 반환한다")
    void getBooks_empty() {
        given(bookRepositoryPort.countByKeyword("없는책")).willReturn(0L);
        given(bookRepositoryPort.findAllByCursor("없는책", "title", Sort.Direction.DESC, null, null, 20))
            .willReturn(List.of());

        CursorPageResponse<BookDto> response =
            bookService.getBooks("없는책", "title", Sort.Direction.DESC, null, null, 20);

        assertThat(response.content()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
        assertThat(response.totalElements()).isZero();
    }
}
