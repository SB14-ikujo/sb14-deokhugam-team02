package com.sprint.deokhugam.book.application.port.in;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.book.adapter.in.web.dto.BookCreateRequest;
import com.sprint.deokhugam.book.adapter.in.web.dto.BookDto;
import com.sprint.deokhugam.book.adapter.in.web.dto.BookInfoDto;
import com.sprint.deokhugam.book.adapter.in.web.mapper.BookMapper;
import com.sprint.deokhugam.book.application.dto.BookSearchResult;
import com.sprint.deokhugam.book.application.dto.DownloadedThumbnail;
import com.sprint.deokhugam.book.application.dto.StoredThumbnail;
import com.sprint.deokhugam.book.application.port.out.BookImageStorage;
import com.sprint.deokhugam.book.application.port.out.BookRepositoryPort;
import com.sprint.deokhugam.book.application.port.out.BookSearchPort;
import com.sprint.deokhugam.book.application.port.out.BookThumbnailDownloadPort;
import com.sprint.deokhugam.book.domain.Book;
import com.sprint.deokhugam.book.domain.exception.BookErrorCode;
import com.sprint.deokhugam.book.domain.exception.BookException;
import com.sprint.deokhugam.book.domain.validator.IsbnValidator;
import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.common.util.SliceUtils;
import com.sprint.deokhugam.user.application.port.out.UserRepositoryPort;
import com.sprint.deokhugam.user.domain.User;
import com.sprint.deokhugam.user.domain.exception.UserErrorCode;
import com.sprint.deokhugam.user.domain.exception.UserException;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService implements BookUseCase {
    private final BookRepositoryPort bookRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final BookMapper bookMapper;
    private final BookSearchPort bookSearchPort;
    private final BookImageStorage bookImageStorage;
    private final BookThumbnailDownloadPort bookThumbnailDownloadPort;

    // 최대 이미지 크기(5MB)
    private static final int MAX_THUMBNAIL_SIZE = 5 * 1024 * 1024;

    // 최대 이미지 픽셀 크기(해상도 제한)
    private static final long MAX_IMAGE_PIXELS = 25_000_000L;

    @Override
    @Transactional
    public BookDto createBook(UUID userId, BookCreateRequest request, MultipartFile thumbnailImage) {
        log.info("도서 등록 요청 : userId : {}", userId);

        // 1. ISBN 정규화 및 유효성 검증
        String isbn = IsbnValidator.normalizeAndValidate(request.isbn());
        log.debug("ISBN 유효성 검증 완료");

        // 2. ISBN 중복 확인
        if (bookRepositoryPort.existsByIsbn(isbn)) {
            log.warn("도서 등록 실패 : ISBN 중복");
            throw new BookException(BookErrorCode.BOOK_ISBN_DUPLICATE);
        }

        // 3. 사용자 확인
        User user = userRepositoryPort.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> {
                log.warn("도서 등록 실패 : 사용자를 찾을 수 없음");
                return new UserException(UserErrorCode.USER_NOT_FOUND);
            });

        // 직접 첨부한 이미지가 있으면 우선 사용
        // 없으면 카카오 썸네일 다운로드
        // 4. 썸네일 저장
        StoredThumbnail thumbnail = storeThumbnail(thumbnailImage, isbn);

        String thumbnailUrl = thumbnail == null ? null : thumbnail.imageKey();

        // 5. 도서 엔티티 생성
        Book book = Book.create(
            request.title(),
            user,
            request.author(),
            request.description(),
            request.publisher(),
            request.publishedDate(),
            isbn,
            thumbnailUrl
        );

        // DB 저장은 성공했지만 URL 생성에 실패한 경우 이미지가 삭제된다.
        // DB 저장 부분만 try-catch로 감싸는게 좋다.
        Book savedBook;

        try {
            // 6. 도서 저장
            savedBook = bookRepositoryPort.saveAndFlush(book);
            log.info("도서 등록 성공 : bookId : {}, userId : {}", savedBook.getId(), userId);
        } catch (RuntimeException e) {
            // DB 저장에 실패하면 이미 저장한 이미지 삭제
            if (thumbnailUrl != null) {
                try {
                    bookImageStorage.delete(thumbnailUrl);
                } catch (RuntimeException cleanException) {
                    log.error("도서 등록 실패 후 썸네일 삭제 실패 : imageKey : {}", thumbnailUrl, cleanException);
                }
            }

            throw e;
        }

        return toBookDto(savedBook);
    }

    @Override
    public BookInfoDto searchBooksByIsbn(String isbn) {
        log.info("ISBN 도서 검색 요청 : isbn : {}", isbn);

        // 1. ISBN 정규화 및 유효성 검증
        String normalizedIsbn = IsbnValidator.normalizeAndValidate(isbn);

        // 2. 카카오 도서 검색
        List<BookSearchResult> results = bookSearchPort.searchByIsbn(normalizedIsbn);
        log.info("ISBN 도서 검색 완료 : isbn : {}, resultCount : {}", normalizedIsbn, results.size());

        // 3. 검색 결과가 없는 경우 예외 발생
        BookSearchResult result = results.stream()
            .findFirst()
            .orElseThrow(() -> {
                log.warn("ISBN 도서 검색 실패 : 검색 결과 없음 : isbn : {}", normalizedIsbn);

                return new BookException(BookErrorCode.BOOK_INFO_NOT_FOUND);
            });

        // 4. 썸네일 이미지 다운로드
        byte[] thumbnailImage = downloadKakaoThumbnail(result.thumbnail(), normalizedIsbn);

        // 5. 검색 결과를 BookInfoDto로 변환
        BookInfoDto bookInfoDto = bookMapper.toInfoDto(result, thumbnailImage);
        log.info("ISBN 도서 정보 조회 성공 : isbn : {}", normalizedIsbn);

        return bookInfoDto;
    }

    /*
     * 사용자 업로드 이미지 또는 카카오 썸네일을 저장한다.
     * 사용자 업로드 이미지가 우선순위가 높다.
     */
    private StoredThumbnail storeThumbnail(
        MultipartFile thumbnailImage,
        String isbn
    ) {
        byte[] imageBytes;
        String contentType;
        String originalFilename;

        // 1. 사용자가 직접 첨부한 이미지
        if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
            try {
                imageBytes = thumbnailImage.getBytes();
            } catch (IOException e) {
                throw new IllegalStateException("첨부한 썸네일을 읽을 수 없습니다.", e);
            }

            contentType = thumbnailImage.getContentType();
            originalFilename = thumbnailImage.getOriginalFilename();

            if (contentType == null || contentType.isBlank()) {
                throw new IllegalArgumentException("썸네일의 콘텐츠 타입을 확인할 수 없습니다.");
            }

            if (originalFilename == null || originalFilename.isBlank()) {
                originalFilename = "thumbnail";
            }
        } else {
            // 2. 직접 첨부한 이미지가 없으면 카카오 검색 결과 활용
            Optional<DownloadedThumbnail> thumbnail = findKakaoThumbnail(isbn);

            if (thumbnail.isEmpty()) {
                log.info("저장할 썸네일 없음 : isbn : {}", isbn);

                return null;
            }

            DownloadedThumbnail downloadedThumbnail = thumbnail.get();

            imageBytes = downloadedThumbnail.bytes();
            contentType = downloadedThumbnail.contentType();
            originalFilename = downloadedThumbnail.originalFilename();
        }

        // 3. 파일명에 확장자가 없거나 MIME 타입과 일치하지 않으면 확장자 보정
        validateImageBytes(imageBytes, contentType);

        originalFilename = ensureFileExtension(originalFilename, contentType);

        // 4. 이미지 저장
        String imageKey = bookImageStorage.generateImageKey(originalFilename);

        String savedImageKey = bookImageStorage.upload(
            imageKey,
            imageBytes,
            contentType
        );

        return new StoredThumbnail(savedImageKey, contentType);
    }

    /*
     * ISBN으로 카카오 도서를 검색하고 썸네일을 다운로드한다.
     * 검색 또는 다운로드 실패 시 빈 Optional을 반환한다.
     */
    private Optional<DownloadedThumbnail> findKakaoThumbnail(String isbn) {
        try {
            return bookSearchPort.searchByIsbn(isbn)
                .stream()
                .map(BookSearchResult::thumbnail)
                .filter(url -> url != null && !url.isBlank())
                .findFirst()
                .flatMap(bookThumbnailDownloadPort::download);
        } catch (RuntimeException e) {
            log.warn("카카오 썸네일 조회 실패 : isbn : {}", isbn);

            return Optional.empty();
        }
    }

    /*
     * 카카오 도서 검색 결과의 썸네일을 다운로드한다.
     * 다운로드 실패 시 null을 반환한다.
     */
    private byte[] downloadKakaoThumbnail(String thumbnailUrl, String isbn) {
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            log.info("조회할 썸네일 없음 : isbn : {}", isbn);

            return null;
        }

        try {
            Optional<DownloadedThumbnail> thumbnail = bookThumbnailDownloadPort.download(thumbnailUrl);

            if (thumbnail.isEmpty()) {
                log.warn("카카오 썸네일 다운로드 결과 없음 : isbn : {}", isbn);

                return null;
            }

            DownloadedThumbnail downloadedThumbnail = thumbnail.get();

            // 기존 이미지 검증 로직 재사용
            validateImageBytes(
                downloadedThumbnail.bytes(),
                downloadedThumbnail.contentType()
            );
            log.info("카카오 썸네일 다운로드 성공 : isbn : {}", isbn);

            return downloadedThumbnail.bytes();
        } catch (RuntimeException e) {
            log.warn("카카오 썸네일 다운로드 실패 : isbn : {}", isbn, e);

            return null;
        }
    }

    /*
     * MIME 타입을 기준으로 파일 확장자를 검증하고 보정한다.
     */
    private String ensureFileExtension(String filename, String contentType) {
        String normalizedContentType = contentType.split(";")[0].trim().toLowerCase();

        String expectedExtension = switch (normalizedContentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다.");
        };

        int extensionIndex = filename.lastIndexOf('.');

        // 확장자가 없는 파일명
        if (extensionIndex <= 0) {
            return filename + expectedExtension;
        }

        String currentExtension = filename.substring(extensionIndex).toLowerCase();

        // JPEG는 .jpg와 .jpeg를 모두 허용
        if (normalizedContentType.equals("image/jpeg")
            && (currentExtension.equals(".jpg") || currentExtension.equals(".jpeg"))) {
            return filename;
        }

        // MIME 타입과 확장자가 일치하는 경우
        if (currentExtension.equals(expectedExtension)) {
            return filename;
        }

        // 확장자가 일치하지 않으면 MIME 타입에 맞게 교체
        return filename.substring(0, extensionIndex) + expectedExtension;
    }

    /*
     * 저장된 이미지 키의 확장자를 기준으로 MIME 타입을 판별한다.
     */
    private String resolveContentType(String imageKey) {
        int extensionIndex = imageKey.lastIndexOf('.');

        if (extensionIndex == -1) {
            return "application/octet-stream";
        }

        String extension = imageKey.substring(imageKey.lastIndexOf('.') + 1).toLowerCase();

        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            default -> "application/octet-stream";
        };
    }

    /*
     * Book 엔티티를 DTO로 변환하면서 실제 이미지 URL을 생성한다.
     */
    private BookDto toBookDto(Book book) {
        BookDto bookDto = bookMapper.toDto(book);

        String imageKey = book.getThumbnailUrl();

        if (imageKey == null || imageKey.isBlank()) {
            return bookDto;
        }

        String contentType = resolveContentType(imageKey);
        String imageUrl = bookImageStorage.getImageUrl(imageKey, contentType);

        return new BookDto(
            bookDto.id(),
            bookDto.title(),
            bookDto.author(),
            bookDto.description(),
            bookDto.publisher(),
            bookDto.publishedDate(),
            bookDto.isbn(),
            imageUrl,
            bookDto.reviewCount(),
            bookDto.rating(),
            bookDto.createdAt(),
            bookDto.updatedAt()
        );
    }

    /*
     * 이미지 바이트 검증 메서드
     */
    private void validateImageBytes(byte[] imageBytes, String contentType) {
        // 1. 이미지 파일이 비어 있는지 확인
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("이미지 파일이 비어 있습니다.");
        }

        // 2. 이미지 크기 확인
        if (imageBytes.length > MAX_THUMBNAIL_SIZE) {
            throw new IllegalArgumentException("이미지 파일 크기는 5MB를 초과할 수 없습니다.");
        }

        // 3. Content-Type 정규화
        String normalizedContentType = contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);

        String detectedContentType = detectImageContentType(imageBytes);

        // 4. 실제 이미지 형식 확인
        if (!detectedContentType.equals(normalizedContentType)) {
            throw new IllegalArgumentException(
                "이미지 파일의 실제 형식과 Content-Type이 일치하지 않습니다."
            );
        }

        if (!detectedContentType.equals(contentType)) {
            throw new IllegalArgumentException("이미지 파일 형식이 Content-Type과 일치하지 않습니다.");
        }

        validateImageDecodable(imageBytes);
    }

    private void validateImageDecodable(byte[] imageBytes) {
        try (
            ImageInputStream inputStream = ImageIO.createImageInputStream(
                new ByteArrayInputStream(imageBytes)
            )
        ) {
            if (inputStream == null) {
                throw new IllegalArgumentException("이미지 파일을 읽을 수 없습니다.");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);

            if (!readers.hasNext()) {
                throw new IllegalArgumentException("지원하지 않거나 손상된 이미지 파일입니다.");
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(inputStream, true, true);

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width <= 0 || height <= 0
                    || (long)width * height > MAX_IMAGE_PIXELS) {
                    throw new IllegalArgumentException("이미지 해상도가 허용 범위를 초과했습니다.");
                }

                BufferedImage image = reader.read(0);

                if (image == null) {
                    throw new IllegalArgumentException("손상된 이미지 파일입니다.");
                }
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("이미지 파일을 디코딩할 수 없습니다.", e);
        }
    }

    private String detectImageContentType(byte[] bytes) {
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (bytes.length >= 8
            && (bytes[0] & 0xFF) == 0x89
            && bytes[1] == 0x50
            && bytes[2] == 0x4E
            && bytes[3] == 0x47
            && bytes[4] == 0x0D
            && bytes[5] == 0x0A
            && bytes[6] == 0x1A
            && bytes[7] == 0x0A) {
            return "image/png";
        }

        // JPEG: FF D8 FF
        if (bytes.length >= 3
            && (bytes[0] & 0xFF) == 0xFF
            && (bytes[1] & 0xFF) == 0xD8
            && (bytes[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }

        // GIF: GIF87a 또는 GIF89a
        if (bytes.length >= 6
            && bytes[0] == 'G'
            && bytes[1] == 'I'
            && bytes[2] == 'F'
            && bytes[3] == '8'
            && (bytes[4] == '7' || bytes[4] == '9')
            && bytes[5] == 'a') {
            return "image/gif";
        }

        // WebP: RIFF....WEBP
        if (bytes.length >= 12
            && bytes[0] == 'R'
            && bytes[1] == 'I'
            && bytes[2] == 'F'
            && bytes[3] == 'F'
            && bytes[8] == 'W'
            && bytes[9] == 'E'
            && bytes[10] == 'B'
            && bytes[11] == 'P') {
            return "image/webp";
        }

        throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다.");
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<BookDto> getBooks(
        String keyword, String orderBy, Sort.Direction direction,
        String cursor, Instant after, Integer limit
    ) {

        long bookTotalCount = bookRepositoryPort.countByKeyword(keyword);
        log.info("도서 목록 조회 정보 -keyword: {}, orderBy:{}, direction:{}, cursor:{}, after:{}, limit:{}",
            keyword, orderBy, direction, cursor, after, limit
        );

        this.validateCursor(orderBy, cursor);

        List<BookDto> books = bookRepositoryPort
            .findAllByCursor(keyword, orderBy, direction, cursor, after, limit)
            .stream().map(bookMapper::toDto)
            .toList();

        Slice<BookDto> slice = SliceUtils.toSlice(books, limit);

        Function<BookDto, String> cursorExtractor = switch (orderBy) {
            case "publishedDate" -> dto -> dto.publishedDate().toString();  // 출판일순
            case "rating" -> dto -> String.valueOf(dto.rating());           // 평점
            case "reviewCount" -> dto -> String.valueOf(dto.reviewCount()); // 리뷰
            default -> BookDto::title;                                      // 제목순
        };

        log.info("총 데이터 : {}", bookTotalCount);
        log.info(
            "최종 데이터 정보 [content_size: {} hasNext : {}]",
            slice.getNumberOfElements(), slice.hasNext()
        );

        return CursorPageResponse.of(
            slice, bookTotalCount,
            cursorExtractor,
            BookDto::createdAt
        );
    }

    private void validateCursor(String orderBy, String cursor) {
        if (cursor == null) {
            return;
        }
        try {
            switch (orderBy) {
                case "publishedDate" -> LocalDate.parse(cursor);
                case "rating" -> Double.parseDouble(cursor);
                case "reviewCount" -> Integer.parseInt(cursor);
                default -> {
                }
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            log.warn("도서 목록 조회 실패 : 잘못된 cursor - orderBy: {}, cursor: {}", orderBy, cursor);
            throw new BookException(BookErrorCode.BOOK_INVALID_CURSOR);
        }
    }
}
