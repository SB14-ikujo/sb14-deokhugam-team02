package com.sprint.deokhugam.user.application.port.in;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.AdditionalAnswers.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sprint.deokhugam.user.adapter.in.web.dto.UserDto;
import com.sprint.deokhugam.user.adapter.in.web.dto.UserRegisterRequest;
import com.sprint.deokhugam.user.adapter.in.web.mapper.UserMapper;
import com.sprint.deokhugam.user.application.port.out.PasswordHasher;
import com.sprint.deokhugam.user.application.port.out.UserRepositoryPort;
import com.sprint.deokhugam.user.domain.User;
import com.sprint.deokhugam.user.domain.exception.UserErrorCode;
import com.sprint.deokhugam.user.domain.exception.UserException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String EMAIL = "user@naver.com";
    private static final String NICKNAME = "닉네임";
    private static final String RAW_PASSWORD = "abcd123!";
    private static final String HASHED_PASSWORD = "hashed-password";

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입하면 비밀번호를 해시해서 저장하고 UserDto를 반환한다.")
    void register_success() {
        // given
        UserRegisterRequest request = new UserRegisterRequest(EMAIL, NICKNAME, RAW_PASSWORD);
        UserDto expected = new UserDto(UUID.randomUUID(), EMAIL, NICKNAME, Instant.now());
        given(userRepository.existsByEmail(EMAIL)).willReturn(false);
        given(userRepository.existsByNickname(NICKNAME)).willReturn(false);
        given(passwordHasher.hash(RAW_PASSWORD)).willReturn(HASHED_PASSWORD);
        given(userRepository.save(any(User.class))).willAnswer(returnsFirstArg());
        given(userMapper.toDto(any(User.class))).willReturn(expected);

        // when
        UserDto result = userService.register(request);

        // then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        then(userRepository).should().save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo(HASHED_PASSWORD);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("이미 가입된 이메일이면 USER_EMAIL_DUPLICATE 예외가 발생하고 저장하지 않는다.")
    void register_duplicateEmail() {
        // given
        UserRegisterRequest request = new UserRegisterRequest(EMAIL, NICKNAME, RAW_PASSWORD);
        given(userRepository.existsByEmail(EMAIL)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.register(request))
            .isInstanceOf(UserException.class)
            .extracting("errorCode")
            .isEqualTo(UserErrorCode.USER_EMAIL_DUPLICATE);
        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("이미 사용 중인 닉네임이면 USER_NICKNAME_DUPLICATE 예외가 발생하고 저장하지 않는다.")
    void register_duplicateNickname() {
        // given
        UserRegisterRequest request = new UserRegisterRequest(EMAIL, NICKNAME, RAW_PASSWORD);
        given(userRepository.existsByEmail(EMAIL)).willReturn(false);
        given(userRepository.existsByNickname(NICKNAME)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.register(request))
            .isInstanceOf(UserException.class)
            .extracting("errorCode")
            .isEqualTo(UserErrorCode.USER_NICKNAME_DUPLICATE);
        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("이메일은 앞뒤 공백을 제거하고 소문자로 바꿔서 중복 확인과 저장에 사용한다.")
    void register_normalizeEmail() {
        // given
        UserRegisterRequest request = new UserRegisterRequest("  User@Naver.COM ", NICKNAME, RAW_PASSWORD);
        given(userRepository.existsByEmail(EMAIL)).willReturn(false);
        given(userRepository.existsByNickname(NICKNAME)).willReturn(false);
        given(passwordHasher.hash(RAW_PASSWORD)).willReturn(HASHED_PASSWORD);
        given(userRepository.save(any(User.class))).willAnswer(returnsFirstArg());

        // when
        userService.register(request);

        // then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        then(userRepository).should().save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo(EMAIL);
    }
}
