package com.sprint.deokhugam.review.adapter.in.web;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sprint.deokhugam.common.pagination.CursorPageResponse;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewCreateRequest;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewDto;
import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewListRequest;
import com.sprint.deokhugam.review.application.port.in.ReviewUseCase;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewUseCase reviewUseCase;

    private String body(String bookId, String userId, String content, int rating) {
        return """
            {
                "bookId": %s,
                "userId": %s,
                "content": %s,
                "rating": %d
            }
            """.formatted(bookId, userId, content, rating);
    }

    private String updateBody(String content, int rating) {
        return """
            {
                "content": %s,
                "rating": %d
            }
            """.formatted(content, rating);
    }

    private String quoted(Object value) {
        return "\"" + value + "\"";
    }

    @Test
    @DisplayName("리뷰 생성 API 성공: 올바른 데이터로 요청하면 201 Created와 생성된 리뷰 DTO를 반환한다.")
    void createReview_Success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        ReviewCreateRequest request = new ReviewCreateRequest(userId, bookId, "진짜 너무 재미있어요!", 5);

        ReviewDto mockResponse = new ReviewDto(
            reviewId, bookId, "도서 제목", "https://thumbnail.url",
            userId, "수달짱", "진짜 너무 재미있어요!", 5, 0, 0, false,
            Instant.now(), Instant.now()
        );

        given(reviewUseCase.createReview(any(ReviewCreateRequest.class))).willReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(quoted(bookId), quoted(userId), quoted("진짜 너무 재미있어요!"), 5)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.content").value("진짜 너무 재미있어요!"))
            .andExpect(jsonPath("$.rating").value(5))
            .andExpect(jsonPath("$.userNickname").value("수달짱"));
    }

    @Test
    @DisplayName("리뷰 생성 API 실패: 평점이 허용된 범위(1~5)를 초과하면 컨트롤러 입구(@Valid)에서 400 Bad Request로 차단한다.")
    void createReview_Fail_InvalidRating_Over() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        String invalidRequestJson = body(
            quoted(bookId),
            quoted(userId),
            quoted("평점 공격 시도!"),
            100
        );

        // when & then
        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestJson))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewUseCase);
    }

    @Test
    @DisplayName("리뷰 논리삭제 API 성공: 본인 리뷰 삭제 요청 시 리턴 바디가 없는 204 NoContent 검증")
    void reviewSoftDelete_Success() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
                .header("Deokhugam-Request-User-ID", userId.toString()))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("리뷰 물리삭제 API 성공: 물리 삭제 요청 시 리턴 바디가 없는 204 NoContent 검증")
    void reviewHardDelete_Success() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/reviews/{reviewId}/hard", reviewId)
                .header("Deokhugam-Request-User-ID", userId.toString()))
            .andExpect(status().isNoContent());
    }

    //
    @Test
    @DisplayName("리뷰 수정 API 성공: 정상 수정 요청 시 200 OK 와 수정 완료된 DTO JSON 반환 검증")
    void updateReview_Success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        String requestJson = updateBody(quoted("내용을 싹 고쳤습니다!"), 4);

        ReviewDto mockResponse = new ReviewDto(
            reviewId, bookId, "도서제목", "https://thumbnail.url",
            userId, "수달짱", "내용을 싹 고쳤습니다!", 4, 0, 0, false,
            Instant.now(), Instant.now()
        );

        given(reviewUseCase.updateReview(eq(reviewId), any(UUID.class), any())).willReturn(mockResponse);

        // when & then
        mockMvc.perform(patch("/api/reviews/{reviewId}", reviewId)
                .header("Deokhugam-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("내용을 싹 고쳤습니다!"))
            .andExpect(jsonPath("$.rating").value(4))
            .andExpect(jsonPath("$.userNickname").value("수달짱"));
    }

    @Test
    @DisplayName("리뷰 수정 API 실패: 수정 데이터의 유효성 검증 실패 시 입구 컷 400 Bad Request 검증")
    void updateReview_Fail_InvalidInput() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        String invalidRequestJson = updateBody(quoted("평점 공격"), 999);

        // when & then
        mockMvc.perform(patch("/api/reviews/{reviewId}", reviewId)
                .header("Deokhugam-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestJson))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(reviewUseCase);
    }

    //
    @Test
    @DisplayName("리뷰 상세 조회 API 성공: 리뷰 Id와 유저 Id를 주었을 때 200 OK 와 상세 내용 JSON 검증")
    void getReviewDetail_Success() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ReviewDto mockResponse = new ReviewDto(
            reviewId, bookId, "도서 제목", "https://thumbnail.url",
            userId, "수달짱", "진짜 너무 재미있어요!", 5, 0, 0, false,
            Instant.now(), Instant.now()
        );

        given(reviewUseCase.getReviewDetail(reviewId, userId)).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/reviews/{reviewId}", reviewId)
                .header("Deokhugam-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(reviewId.toString()))
            .andExpect(jsonPath("$.content").value("진짜 너무 재미있어요!"))
            .andExpect(jsonPath("$.rating").value(5))
            .andExpect(jsonPath("$.userNickname").value("수달짱"));
    }

    @Test
    @DisplayName("리뷰 목록 조회 API 성공: 페이징 쿼리 파라미터가 들어왔을 때 무한 스크롤 구조의 200 OK 커서 레코드 JSON 검증")
    void getList_Success() throws Exception {
        // given
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        ReviewDto mockReviewDto = new ReviewDto(
            reviewId, bookId, "도서 제목", "https://thumbnail.url",
            userId, "수달짱", "목록에 나올 리뷰 내용입니다.", 5, 0, 0, false,
            Instant.now(), Instant.now()
        );

        List<ReviewDto> reviewList = List.of(mockReviewDto);
        String nextCursor = "2026-11-04T00:00:00.000Z";
        Instant nextAfter = Instant.parse(nextCursor);
        int size = 10;
        long totalElements = 1L;
        boolean hasNext = true;

        CursorPageResponse<ReviewDto> mockPageResponse = new CursorPageResponse<>(reviewList, nextCursor, nextAfter,
            size, totalElements, hasNext);

        given(reviewUseCase.getReviewList(any(ReviewListRequest.class), eq(userId))).willReturn(mockPageResponse);

        // when & then
        mockMvc.perform(get("/api/reviews")
                .param("bookId", bookId.toString())
                .param("size", "10")
                .header("Deokhugam-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(reviewId.toString()))
            .andExpect(jsonPath("$.content[0].content").value("목록에 나올 리뷰 내용입니다."))
            .andExpect(jsonPath("$.content[0].userNickname").value("수달짱"))
            .andExpect(jsonPath("$.nextCursor").value(nextCursor))
            .andExpect(jsonPath("$.hasNext").value(hasNext));
    }
}
