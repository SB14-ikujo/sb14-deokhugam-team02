package com.sprint.deokhugam.comment;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sprint.deokhugam.comment.adapter.in.web.CommentController;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentCreateRequest;
import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentDto;
import com.sprint.deokhugam.comment.application.port.in.CommentUseCase;
import com.sprint.deokhugam.comment.domain.exception.CommentErrorCode;
import com.sprint.deokhugam.comment.domain.exception.CommentException;

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
}
