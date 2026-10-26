package com.sprint.deokhugam.user.application.port.in;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.AdditionalAnswers.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.sprint.deokhugam.user.adapter.in.web.dto.UserDto;
import com.sprint.deokhugam.user.adapter.in.web.dto.UserLoginRequest;
import com.sprint.deokhugam.user.adapter.in.web.dto.UserRegisterRequest;
import com.sprint.deokhugam.user.adapter.in.web.dto.UserUpdateRequest;
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
    private static final String NEW_NICKNAME = "새로운닉네임";

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
        given(userRepository.saveAndFlush(any(User.class))).willAnswer(returnsFirstArg());
        given(userMapper.toDto(any(User.class))).willReturn(expected);

        // when
        UserDto result = userService.register(request);

        // then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        then(userRepository).should().saveAndFlush(captor.capture());
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
        then(userRepository).should(never()).saveAndFlush(any(User.class));
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
        then(userRepository).should(never()).saveAndFlush(any(User.class));
    }

    @Test
    @DisplayName("이메일은 앞뒤 공백을 제거하고 소문자로 바꿔서 중복 확인과 저장에 사용한다.")
    void register_normalizeEmail() {
        // given
        UserRegisterRequest request = new UserRegisterRequest("  User@Naver.COM ", NICKNAME, RAW_PASSWORD);
        given(userRepository.existsByEmail(EMAIL)).willReturn(false);
        given(userRepository.existsByNickname(NICKNAME)).willReturn(false);
        given(passwordHasher.hash(RAW_PASSWORD)).willReturn(HASHED_PASSWORD);
        given(userRepository.saveAndFlush(any(User.class))).willAnswer(returnsFirstArg());

        // when
        userService.register(request);

        // then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        then(userRepository).should().saveAndFlush(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("동시 가입으로 DB 유니크 제약에 걸리면 USER_DUPLICATE 예외를 반환")
    void register_concurrentDuplicate() {
        // given
        UserRegisterRequest request = new UserRegisterRequest(EMAIL, NICKNAME, RAW_PASSWORD);
        given(userRepository.existsByEmail(EMAIL)).willReturn(false);
        given(userRepository.existsByNickname(NICKNAME)).willReturn(false);
        given(passwordHasher.hash(RAW_PASSWORD)).willReturn(HASHED_PASSWORD);
        given(userRepository.saveAndFlush(any(User.class)))
            .willThrow(new DataIntegrityViolationException("duplicate key"));

        // when & then
        assertThatThrownBy(() -> userService.register(request))
            .isInstanceOf(UserException.class)
            .extracting("errorCode")
            .isEqualTo(UserErrorCode.USER_DUPLICATE);
    }

    @Test
    @DisplayName("이메일과 비밀번호가 맞으면 UserDto를 반환")
    void login_success() {
        // given
        UserLoginRequest request = new UserLoginRequest(EMAIL, RAW_PASSWORD);
        User user = User.create(EMAIL, NICKNAME, HASHED_PASSWORD);
        UserDto expected = new UserDto(UUID.randomUUID(), EMAIL, NICKNAME, Instant.now());
        given(userRepository.findByEmailAndIsDeletedFalse(EMAIL)).willReturn(Optional.of(user));
        given(passwordHasher.matches(RAW_PASSWORD, HASHED_PASSWORD)).willReturn(true);
        given(userMapper.toDto(user)).willReturn(expected);

        // when
        UserDto result = userService.login(request);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("가입되지 않은 이메일이면 USER_LOGIN_FAILED 예외가 발생한다.")
    void login_emailNotFound() {
        // given
        UserLoginRequest request = new UserLoginRequest(EMAIL, RAW_PASSWORD);
        given(userRepository.findByEmailAndIsDeletedFalse(EMAIL)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.login(request))
            .isInstanceOf(UserException.class)
            .extracting("errorCode")
            .isEqualTo(UserErrorCode.USER_LOGIN_FAILED);
        then(passwordHasher).should(never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("비밀번호가 틀리면 가입되지 않은 이메일과 같은 USER_LOGIN_FAILED 예외가 발생한다.")
    void login_wrongPassword() {
        // given
        UserLoginRequest request = new UserLoginRequest(EMAIL, "wrong123!");
        User user = User.create(EMAIL, NICKNAME, HASHED_PASSWORD);
        given(userRepository.findByEmailAndIsDeletedFalse(EMAIL)).willReturn(Optional.of(user));
        given(passwordHasher.matches("wrong123!", HASHED_PASSWORD)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.login(request))
            .isInstanceOf(UserException.class)
            .extracting("errorCode")
            .isEqualTo(UserErrorCode.USER_LOGIN_FAILED);
    }

    @Test
    @DisplayName("이메일은 정규화해서 삭제되지 않은 사용자 중에서 조회한다.")
    void login_normalizeEmail() {
        // given
        UserLoginRequest request = new UserLoginRequest("   User@Naver.COM ", RAW_PASSWORD);
        User user = User.create(EMAIL, NICKNAME, HASHED_PASSWORD);
        given(userRepository.findByEmailAndIsDeletedFalse(EMAIL)).willReturn(Optional.of(user));
        given(passwordHasher.matches(RAW_PASSWORD, HASHED_PASSWORD)).willReturn(true);

        // when
        userService.login(request);

        // then
        then(userRepository).should().findByEmailAndIsDeletedFalse(EMAIL);
    }

    @Test
    @DisplayName("존재하는 사용자를 조회하면 UserDto를 반환한다.")
    void findUser_success() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.create(EMAIL, NICKNAME, HASHED_PASSWORD);
        UserDto expected = new UserDto(userId, EMAIL, NICKNAME, Instant.now());
        given(userRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(user));
        given(userMapper.toDto(user)).willReturn(expected);

        // when
        UserDto result = userService.findUser(userId);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("없거나 논리 삭제된 사용자를 조회하면 USER_NOT_FOUND 예외가 발생한다.")
    void findUser_notFound() {
        // given
        UUID userId = UUID.randomUUID();
        given(userRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findUser(userId))
            .isInstanceOf(UserException.class)
            .extracting("errorCode")
            .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("본인의 닉네임을 변경하면 변경된 UserDto를 반환한다.")
    void updateNickname_success() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.create(EMAIL, NICKNAME, HASHED_PASSWORD);
        UserDto expected = new UserDto(userId, EMAIL, NEW_NICKNAME, Instant.now());
        given(userRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(user));
        given(userRepository.existsByNickname(NEW_NICKNAME)).willReturn(false);
        given(userRepository.saveAndFlush(user)).willReturn(user);
        given(userMapper.toDto(user)).willReturn(expected);

        // when
        UserDto result = userService.updateNickname(userId, userId, new UserUpdateRequest(NEW_NICKNAME));

        // then
        assertThat(user.getNickname()).isEqualTo(NEW_NICKNAME);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("다른 사용자의 닉네임을 변경하려 하면 USER_FORBIDDEN 예외가 발생하고 조회하지 않는다.")
    void updateNickname_forbidden() {
        // given
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> userService.updateNickname(userId, otherUserId, new UserUpdateRequest(NEW_NICKNAME)))
            .isInstanceOf(UserException.class)
            .extracting("errorCode")
            .isEqualTo(UserErrorCode.USER_FORBIDDEN);
        then(userRepository).should(never()).findByIdAndIsDeletedFalse(any(UUID.class));
    }

    @Test
    @DisplayName("없거나 논리 삭제된 사용자의 닉네임을 변경하려 하면 USER_NOT_FOUND 예외가 발생한다.")
    void updateNickname_notFound() {
        // given
        UUID userId = UUID.randomUUID();
        given(userRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateNickname(userId, userId, new UserUpdateRequest(NEW_NICKNAME)))
            .isInstanceOf(UserException.class)
            .extracting("errorCode")
            .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("이미 사용 중인 닉네임으로 변경하면 USER_NICKNAME_DUPLICATE 예외가 발생하고 닉네임은 유지된다.")
    void updateNickname_duplicate() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.create(EMAIL, NICKNAME, HASHED_PASSWORD);
        given(userRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(user));
        given(userRepository.existsByNickname(NEW_NICKNAME)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.updateNickname(userId, userId, new UserUpdateRequest(NEW_NICKNAME)))
            .isInstanceOf(UserException.class)
            .extracting("errorCode")
            .isEqualTo(UserErrorCode.USER_NICKNAME_DUPLICATE);
        assertThat(user.getNickname()).isEqualTo(NICKNAME);
        then(userRepository).should(never()).saveAndFlush(any(User.class));
    }

    @Test
    @DisplayName("현재와 같은 닉네임으로 변경하면 중복 검사 없이 그대로 반환한다.")
    void updateNickname_sameNickname() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.create(EMAIL, NICKNAME, HASHED_PASSWORD);
        UserDto expected = new UserDto(userId, EMAIL, NICKNAME, Instant.now());
        given(userRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(user));
        given(userMapper.toDto(user)).willReturn(expected);

        // when
        UserDto result = userService.updateNickname(userId, userId, new UserUpdateRequest(NICKNAME));

        // then
        assertThat(result).isEqualTo(expected);
        then(userRepository).should(never()).existsByNickname(anyString());
        then(userRepository).should(never()).saveAndFlush(any(User.class));
    }

    @Test
    @DisplayName("동시 요청으로 DB unique 제약에 걸리면 USER_NICKNAME_DUPLICATE 예외로 반환한다.")
    void updateNickname_concurrentDuplicate() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.create(EMAIL, NICKNAME, HASHED_PASSWORD);
        given(userRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(user));
        given(userRepository.existsByNickname(NEW_NICKNAME)).willReturn(false);
        given(userRepository.saveAndFlush(user)).willThrow(new DataIntegrityViolationException("duplicate key"));

        // when & then
        assertThatThrownBy(() -> userService.updateNickname(userId, userId, new UserUpdateRequest(NEW_NICKNAME)))
            .isInstanceOf(UserException.class)
            .extracting("errorCode")
            .isEqualTo(UserErrorCode.USER_NICKNAME_DUPLICATE);
    }
}
