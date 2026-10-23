package com.sprint.deokhugam.comment;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sprint.deokhugam.comment.adapter.in.web.CommentController;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentCreateRequest;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentDto;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentListRequest;
import com.sprint.deokhugam.comment.application.port.in.CommentUseCase;
import com.sprint.deokhugam.comment.domain.exception.CommentErrorCode;
import com.sprint.deokhugam.comment.domain.exception.CommentException;
import com.sprint.deokhugam.common.pagination.CursorPageResponse;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentUseCase commentUseCase;

    private final UUID reviewId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    private String body(String reviewId, String userId, String content) {
        return """
            {"reviewId": %s, "userId": %s, "content": %s}
            """.formatted(reviewId, userId, content);
    }

    private String quoted(Object value) {
        return "\"" + value + "\"";
    }

    @Test
    @DisplayName("댓글 등록 성공 - 201과 CommentDto 반환")
    void create_success() throws Exception {
        CommentDto dto = new CommentDto(UUID.randomUUID(), reviewId, userId, "닉네임",
            "좋은 리뷰네요!", Instant.now(), Instant.now());
        given(commentUseCase.create(any(CommentCreateRequest.class))).willReturn(dto);

        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(quoted(reviewId), quoted(userId), quoted("좋은 리뷰네요!"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(dto.id().toString()))
            .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
            .andExpect(jsonPath("$.userId").value(userId.toString()))
            .andExpect(jsonPath("$.userNickname").value("닉네임"))
            .andExpect(jsonPath("$.content").value("좋은 리뷰네요!"));
    }

    @Test
    @DisplayName("reviewId 누락 시 400")
    void create_reviewIdNull() throws Exception {
        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("null", quoted(userId), quoted("내용"))))
            .andExpect(status().isBadRequest());

        verify(commentUseCase, never()).create(any());
    }

    @Test
    @DisplayName("userId 누락 시 400")
    void create_userIdNull() throws Exception {
        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(quoted(reviewId), "null", quoted("내용"))))
            .andExpect(status().isBadRequest());

        verify(commentUseCase, never()).create(any());
    }

    @Test
    @DisplayName("content가 공백이면 400")
    void create_contentBlank() throws Exception {
        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(quoted(reviewId), quoted(userId), quoted("   "))))
            .andExpect(status().isBadRequest());

        verify(commentUseCase, never()).create(any());
    }

    @Test
    @DisplayName("content가 500자를 초과하면 400")
    void create_contentTooLong() throws Exception {
        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(quoted(reviewId), quoted(userId), quoted("a".repeat(501)))))
            .andExpect(status().isBadRequest());

        verify(commentUseCase, never()).create(any());
    }

    @Test
    @DisplayName("UUID 형식이 잘못되면 400")
    void create_invalidUuid() throws Exception {
        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(quoted("not-a-uuid"), quoted(userId), quoted("내용"))))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리뷰가 없으면 404")
    void create_reviewNotFound() throws Exception {
        given(commentUseCase.create(any(CommentCreateRequest.class)))
            .willThrow(new CommentException(CommentErrorCode.COMMENT_REVIEW_NOT_FOUND));

        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(quoted(reviewId), quoted(userId), quoted("내용"))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("리뷰 정보를 찾을 수 없습니다."));
    }

    // ===== 댓글 목록 조회 =====

    @Test
    @DisplayName("목록 조회 성공 - 200과 CursorPageResponse 반환")
    void getList_success() throws Exception {
        UUID commentId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
        CommentDto dto = new CommentDto(commentId, reviewId, userId, "닉네임", "댓글", createdAt, createdAt);
        given(commentUseCase.getList(any(CommentListRequest.class)))
            .willReturn(new CursorPageResponse<>(List.of(dto), commentId.toString(), createdAt, 1, 3L, true));

        mockMvc.perform(get("/api/comments").param("reviewId", reviewId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(commentId.toString()))
            .andExpect(jsonPath("$.content[0].content").value("댓글"))
            .andExpect(jsonPath("$.nextCursor").value(commentId.toString()))
            .andExpect(jsonPath("$.size").value(1))
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    @DisplayName("목록 조회 - direction·limit을 생략하면 DESC, 50이 기본값")
    void getList_defaults() throws Exception {
        given(commentUseCase.getList(any(CommentListRequest.class)))
            .willReturn(new CursorPageResponse<>(List.of(), null, null, 0, 0L, false));

        mockMvc.perform(get("/api/comments").param("reviewId", reviewId.toString()))
            .andExpect(status().isOk());

        ArgumentCaptor<CommentListRequest> captor = ArgumentCaptor.forClass(CommentListRequest.class);
        verify(commentUseCase).getList(captor.capture());
        assertThat(captor.getValue().direction()).isEqualTo(Sort.Direction.DESC);
        assertThat(captor.getValue().limit()).isEqualTo(50);
    }

    @Test
    @DisplayName("목록 조회 - 쿼리 파라미터가 요청 객체로 바인딩된다")
    void getList_bindsParams() throws Exception {
        UUID cursorId = UUID.randomUUID();
        given(commentUseCase.getList(any(CommentListRequest.class)))
            .willReturn(new CursorPageResponse<>(List.of(), null, null, 0, 0L, false));

        mockMvc.perform(get("/api/comments")
                .param("reviewId", reviewId.toString())
                .param("direction", "ASC")
                .param("cursor", cursorId.toString())
                .param("after", "2024-01-01T00:00:00Z")
                .param("limit", "10"))
            .andExpect(status().isOk());

        ArgumentCaptor<CommentListRequest> captor = ArgumentCaptor.forClass(CommentListRequest.class);
        verify(commentUseCase).getList(captor.capture());
        CommentListRequest request = captor.getValue();
        assertThat(request.reviewId()).isEqualTo(reviewId);
        assertThat(request.direction()).isEqualTo(Sort.Direction.ASC);
        assertThat(request.cursor()).isEqualTo(cursorId.toString());
        assertThat(request.after()).isEqualTo(Instant.parse("2024-01-01T00:00:00Z"));
        assertThat(request.limit()).isEqualTo(10);
    }

    @Test
    @DisplayName("목록 조회 - reviewId 누락 시 400")
    void getList_reviewIdMissing() throws Exception {
        mockMvc.perform(get("/api/comments"))
            .andExpect(status().isBadRequest());

        verify(commentUseCase, never()).getList(any());
    }

    @Test
    @DisplayName("목록 조회 - reviewId가 UUID 형식이 아니면 400")
    void getList_invalidReviewId() throws Exception {
        mockMvc.perform(get("/api/comments").param("reviewId", "not-a-uuid"))
            .andExpect(status().isBadRequest());

        verify(commentUseCase, never()).getList(any());
    }

    @Test
    @DisplayName("목록 조회 - direction이 ASC/DESC가 아니면 400")
    void getList_invalidDirection() throws Exception {
        mockMvc.perform(get("/api/comments")
                .param("reviewId", reviewId.toString())
                .param("direction", "UP"))
            .andExpect(status().isBadRequest());

        verify(commentUseCase, never()).getList(any());
    }

    @Test
    @DisplayName("목록 조회 - limit이 범위(1~100)를 벗어나면 400")
    void getList_invalidLimit() throws Exception {
        mockMvc.perform(get("/api/comments")
                .param("reviewId", reviewId.toString())
                .param("limit", "0"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/comments")
                .param("reviewId", reviewId.toString())
                .param("limit", "101"))
            .andExpect(status().isBadRequest());

        verify(commentUseCase, never()).getList(any());
    }

    @Test
    @DisplayName("목록 조회 - cursor 형식이 잘못되면 400")
    void getList_invalidCursor() throws Exception {
        given(commentUseCase.getList(any(CommentListRequest.class)))
            .willThrow(new CommentException(CommentErrorCode.COMMENT_INVALID_CURSOR));

        mockMvc.perform(get("/api/comments")
                .param("reviewId", reviewId.toString())
                .param("cursor", "bad")
                .param("after", "2024-01-01T00:00:00Z"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("잘못된 커서 값입니다."));
    }

    @Test
    @DisplayName("목록 조회 - 리뷰가 없으면 404")
    void getList_reviewNotFound() throws Exception {
        given(commentUseCase.getList(any(CommentListRequest.class)))
            .willThrow(new CommentException(CommentErrorCode.COMMENT_REVIEW_NOT_FOUND));

        mockMvc.perform(get("/api/comments").param("reviewId", reviewId.toString()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("리뷰 정보를 찾을 수 없습니다."));
    }
}
