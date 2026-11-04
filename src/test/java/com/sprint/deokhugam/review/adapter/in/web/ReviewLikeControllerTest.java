package com.sprint.deokhugam.review.adapter.in.web;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sprint.deokhugam.review.adapter.in.web.dto.ReviewLikeDto;
import com.sprint.deokhugam.review.application.port.in.ReviewLikeUseCase;

@WebMvcTest(ReviewLikeController.class)
class ReviewLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewLikeUseCase reviewLikeUseCase;

    @Test
    @DisplayName("좋아요 토글 API 성공: 리뷰 ID를 경로 변수로 주면 200 OK와 토글 결과 DTO를 반환한다.")
    void reviewLikeToggle_Success() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ReviewLikeDto mockResponse = new ReviewLikeDto(reviewId, userId, true);

        given(reviewLikeUseCase.reviewLikeToggle(eq(reviewId), any(UUID.class))).willReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/api/reviews/{reviewId}/like", reviewId)
                .header("Deokhugam-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
            .andExpect(jsonPath("$.userId").value(userId.toString()))
            .andExpect(jsonPath("$.liked").value(true));
    }
}
