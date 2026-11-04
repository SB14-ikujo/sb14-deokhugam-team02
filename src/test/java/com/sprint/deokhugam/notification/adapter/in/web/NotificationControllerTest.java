package com.sprint.deokhugam.notification.adapter.in.web;

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

import com.sprint.deokhugam.notification.adapter.in.web.dto.NotificationDto;
import com.sprint.deokhugam.notification.application.port.in.NotificationUseCase;
import com.sprint.deokhugam.notification.domain.exception.NotificationErrorCode;
import com.sprint.deokhugam.notification.domain.exception.NotificationException;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationUseCase notificationUseCase;

    private static final UUID NOTIFICATION_ID = UUID.fromString("50000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID = UUID.fromString("50000000-0000-0000-0000-000000000002");
    private static final String USER_ID_HEADER = "Deokhugam-Request-User-ID";
    private static final String basePath = "/api/notifications";

    private static final NotificationDto NOTIFICATION_DTO = new NotificationDto(
        NOTIFICATION_ID,
        USER_ID,
        UUID.fromString("50000000-0000-0000-0000-000000000003"),
        "리뷰 내용",
        "[닉네임]님이 나의 리뷰를 좋아합니다.",
        true,
        Instant.parse("2026-10-22T00:00:00Z"),
        Instant.parse("2026-10-22T00:00:00Z")
    );

    @Test
    @DisplayName("알림 읽음 처리에 성공하면 200을 반환한다.")
    void markAsRead_success() throws Exception {
        // given
        given(notificationUseCase.markAsRead(NOTIFICATION_ID, USER_ID, true)).willReturn(NOTIFICATION_DTO);

        // when & then
        mockMvc.perform(patch(basePath + "/{notificationId}", NOTIFICATION_ID)
            .header(USER_ID_HEADER, USER_ID)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content("""
                { "confirmed" : true}
                """
            )
        ).andExpect(status().isOk());

        verify(notificationUseCase).markAsRead(NOTIFICATION_ID, USER_ID, true);
    }

    @Test
    @DisplayName("confirmed가 없으면 400을 반환한다.")
    void markAsRead_confirmedMissing() throws Exception {
        mockMvc.perform(patch(basePath + "/{notificationId}", NOTIFICATION_ID)
                .header(USER_ID_HEADER, USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(notificationUseCase);
    }

    @Test
    @DisplayName("요청자 ID 헤더가 없으면 400을 반환한다.")
    void markAsRead_headerMissing() throws Exception {
        mockMvc.perform(patch(basePath + "/{notificationId}", NOTIFICATION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"confirmed": true}
                    """))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(notificationUseCase);
    }

    @Test
    @DisplayName("알림 읽음 전체 처리에 성공하면 204를 반환한다.")
    void markAllAsRead_returns204() throws Exception {

        mockMvc.perform(patch(basePath + "/read-all")
                .header(USER_ID_HEADER, USER_ID))
            .andExpect(status().isNoContent());

        verify(notificationUseCase).markAllAsRead(USER_ID);
    }

    @Test
    @DisplayName("요청한 사용자의 알림이 없으면 404를 반환한다.")
    void markAllAsRead_returns404WhenNotificationsNotFound() throws Exception {
        // given
        willThrow(new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND))
            .given(notificationUseCase).markAllAsRead(USER_ID);

        mockMvc.perform(patch(basePath + "/read-all")
                .header(USER_ID_HEADER, USER_ID))
            .andExpect(status().isNotFound());

        verify(notificationUseCase).markAllAsRead(USER_ID);
    }

    @Test
    @DisplayName("전체 알림 읽음 - 요청자 ID 헤더가 없으면 400을 반환한다.")
    void markAllAsRead_headerMissing() throws Exception {
        mockMvc.perform(patch(basePath + "/read-all"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(notificationUseCase);
    }

    @Test
    @DisplayName("잘못된 요청 파라미터가 전달되면 400을 반환한다.")
    void getNotifications_returns400WhenInvalidParameters() throws Exception {
        mockMvc.perform(get(basePath)
                .header(USER_ID_HEADER, USER_ID)
                .param("userId", USER_ID.toString())
                .param("after", "테스트중입니다만")
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("사용자 정보가 존재하지 않으면 404를 반환한다.")
    void getNotifications_returns404WhenUserNotFound() throws Exception {
        given(notificationUseCase.getNotifications(any(), any(), any(), any(), any()))
            .willThrow(new NotificationException(NotificationErrorCode.NOTIFICATION_USER_NOT_FOUND));

        mockMvc.perform(get(basePath)
                .param("userId", USER_ID.toString()))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("알림 목록 조회에 성공하면 200을 반환한다.")
    void getNotifications_returns200WhenSuccessful() throws Exception {
        String cursor = "2026-10-16T10:09:56.963925Z";

        mockMvc.perform(get(basePath)
                .header(USER_ID_HEADER, USER_ID)
                .param("userId", USER_ID.toString())
                .param("direction", "ASC")
                .param("cursor", cursor)
                .param("after", cursor)
                .param("limit", "10"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("필수 파라미터만 전달해도 알림 목록 조회에 성공하여 200을 반환한다.")
    void getNotifications_returns200WhenOnlyRequiredParametersProvided() throws Exception {
        mockMvc.perform(get(basePath)
                .header(USER_ID_HEADER, USER_ID)
                .param("userId", USER_ID.toString()))
            .andExpect(status().isOk());
    }

}
