package com.sprint.deokhugam.user.adapter.in.web;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sprint.deokhugam.user.adapter.in.web.dto.UserDto;
import com.sprint.deokhugam.user.adapter.in.web.dto.UserLoginRequest;
import com.sprint.deokhugam.user.adapter.in.web.dto.UserRegisterRequest;
import com.sprint.deokhugam.user.adapter.in.web.dto.UserUpdateRequest;
import com.sprint.deokhugam.user.application.port.in.UserUseCase;
import com.sprint.deokhugam.user.domain.exception.UserErrorCode;
import com.sprint.deokhugam.user.domain.exception.UserException;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    private static final String VALID_BODY = """
        {"email": "user@naver.com", "nickname": "닉네임", "password": "abcd123!"}
        """;

    private static final String LOGIN_BODY = """
        {"email": "user@naver.com", "password": "abcd123!"}
        """;

    private static final String USER_ID_HEADER = "Deokhugam-Request-User-ID";
    private static final String UPDATE_BODY = """
        {"nickname": "새로운닉네임"}""";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserUseCase userUseCase;

    @Test
    @DisplayName("회원가입에 성공하면 201과 비밀번호가 없는 사용자 정보를 반환한다.")
    void register_success() throws Exception {
        // given
        UserDto response = new UserDto(UUID.randomUUID(), "user@naver.com", "푸우", Instant.now());
        given(userUseCase.register(any(UserRegisterRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("user@naver.com"))
            .andExpect(jsonPath("$.nickname").value("푸우"))
            .andExpect(jsonPath("$.password").doesNotExist());

    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"email\": \"not-email\", \"nickname\": \"닉네임\", \"password\": \"abcd123!\"}",
        "{\"email\": \"user@naver.com\", \"nickname\": \"닉\", \"password\": \"abcd123!\"}",
        "{\"email\": \"user@naver.com\", \"nickname\": \"닉네임\", \"password\": \"abcd1234\"}"
    })
    @DisplayName("이메일 형식, 닉네임 길이, 비밀번호 규칙을 어기면 400을 반환하고 서비스를 호출하지 않는다.")
    void register_invalidRequest(String body) throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());

        then(userUseCase).should(never()).register(any(UserRegisterRequest.class));
    }

    @Test
    @DisplayName("이미 가입된 이메일이면 409를 반환한다.")
    void register_duplicateEmail() throws Exception {
        // given
        given(userUseCase.register(any(UserRegisterRequest.class)))
            .willThrow(new UserException(UserErrorCode.USER_EMAIL_DUPLICATE));

        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(UserErrorCode.USER_EMAIL_DUPLICATE.getMessage()));
    }

    @Test
    @DisplayName("이미 사용 중인 닉네임이면 409를 반환한다.")
    void register_duplicateNickname() throws Exception {
        // given
        given(userUseCase.register(any(UserRegisterRequest.class)))
            .willThrow(new UserException(UserErrorCode.USER_NICKNAME_DUPLICATE));

        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(UserErrorCode.USER_NICKNAME_DUPLICATE.getMessage()));
    }

    @Test
    @DisplayName("로그인에 성공하면 200과 비밀번호가 없는 사용자 정보를 반환한다.")
    void login_success() throws Exception {
        // given
        UserDto response = new UserDto(UUID.randomUUID(), "user@naver.com", "푸우", Instant.now());
        given(userUseCase.login(any(UserLoginRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(LOGIN_BODY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("user@naver.com"))
            .andExpect(jsonPath("$.nickname").value("푸우"))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"email\": \"\", \"password\": \"abcd123!\"}",
        "{\"email\": \"user@naver.com\", \"password\": \" \"}"
    })
    @DisplayName("이메일이나 비밀번호가 비어 있으면 400을 반환하고 서비스를 호출하지 않는다.")
    void login_blank(String body) throws Exception {
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());

        then(userUseCase).should(never()).login(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("로그인에 실패하면 401과 에러 메세지를 반환한다.")
    void login_failed() throws Exception {
        // given
        given(userUseCase.login(any(UserLoginRequest.class)))
            .willThrow(new UserException(UserErrorCode.USER_LOGIN_FAILED));

        // when & then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(LOGIN_BODY))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value(UserErrorCode.USER_LOGIN_FAILED.getMessage()));
    }

    @Test
    @DisplayName("로그인 요청에는 비밀번호 정규식을 적용하지 않는다.")
    void login_passwordWithoutPattern() throws Exception {
        // given
        String body = "{\"email\": \"user@naver.com\", \"password\": \"abcd1234\"}";

        // when & then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());

        then(userUseCase).should().login(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("존재하는 사용자를 조회하면 200과 비밀번호가 없는 사용자 정보를 반환한다.")
    void findUser_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UserDto response = new UserDto(userId, "user@naver.com", "푸우", Instant.now());
        given(userUseCase.findUser(userId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/users/{userId}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId.toString()))
            .andExpect(jsonPath("$.email").value("user@naver.com"))
            .andExpect(jsonPath("$.nickname").value("푸우"))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("없거나 논리 삭제된 사용자를 조회하면 404를 반환한다.")
    void findUser_notFound() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        given(userUseCase.findUser(userId)).willThrow(new UserException(UserErrorCode.USER_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/users/{userId}", userId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(UserErrorCode.USER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("사용자 ID가 UUID 형식이 아니면 400을 반환하고 서비스를 호출하지 않는다.")
    void findUser_invalidId() throws Exception {
        mockMvc.perform(get("/api/users/{userId}", "not-a-uuid"))
            .andExpect(status().isBadRequest());

        then(userUseCase).should(never()).findUser(any(UUID.class));
    }

    @Test
    @DisplayName("본인의 닉네임을 변경하면 200과 변경된 사용자 정보를 반환한다.")
    void updateNickname_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UserDto response = new UserDto(userId, "user@naver.com", "새로운닉네임", Instant.now());
        given(userUseCase.updateNickname(eq(userId), eq(userId), any(UserUpdateRequest.class)))
            .willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/users/{userId}", userId)
                .header(USER_ID_HEADER, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(UPDATE_BODY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nickname").value("새로운닉네임"))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("닉네임이 2자 미만이면 400을 반환하고 서비스를 호출하지 않는다.")
    void updateNickname_invalidNickname() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(patch("/api/users/{userId}", userId)
                .header(USER_ID_HEADER, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\": \"닉\"}"))
            .andExpect(status().isBadRequest());

        then(userUseCase).should(never()).updateNickname(any(), any(), any());
    }

    @Test
    @DisplayName("요청자 헤더가 없으면 400을 반환하고 서비스를 호출하지 않는다.")
    void updateNickname_missingHeader() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(patch("/api/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(UPDATE_BODY))
            .andExpect(status().isBadRequest());

        then(userUseCase).should(never()).updateNickname(any(), any(), any());
    }

    @Test
    @DisplayName("다른 사용자의 닉네임을 변경하려 하면 403을 반환한다.")
    void updateNickname_forbidden() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        given(userUseCase.updateNickname(eq(userId), eq(otherUserId), any(UserUpdateRequest.class)))
            .willThrow(new UserException(UserErrorCode.USER_FORBIDDEN));

        // when & then
        mockMvc.perform(patch("/api/users/{userId}", userId)
                .header(USER_ID_HEADER, otherUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(UPDATE_BODY))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value(UserErrorCode.USER_FORBIDDEN.getMessage()));
    }
}
