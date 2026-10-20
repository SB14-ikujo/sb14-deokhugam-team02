package com.sprint.deokhugam.user.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.sprint.deokhugam.user.domain.exception.UserErrorCode;
import com.sprint.deokhugam.user.domain.exception.UserException;

public class UserTest {

    private static final String EMAIL = "user@naver.com";
    private static final String ENCODED_PASSWORD = "password";

    @ParameterizedTest
    @ValueSource(strings = {"ab", "abcdefghijklmnopqrst"})
    @DisplayName("닉네임이 2자 이상 20자 이하이면 사용자를 생성한다.")
    void create_validNickname(String nickname) {
        User user = User.create(EMAIL, nickname, ENCODED_PASSWORD);

        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getPassword()).isEqualTo(ENCODED_PASSWORD);
        assertThat(user.isDeleted()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "abcdefghijklmnopqrstu"})
    @DisplayName("닉네임이 2자 미만이거나 20자를 초과하면 사용자 생성에 실패한다.")
    void create_validNicknameLength(String nickname) {
        assertThatThrownBy(() -> User.create(EMAIL, nickname, ENCODED_PASSWORD))
            .isInstanceOf(UserException.class)
            .extracting("errorCode")
            .isEqualTo(UserErrorCode.USER_NICKNAME_INVALID);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t\t"})
    @DisplayName("닉네임이 null이거나 공백이면 사용자 생성에 실패한다.")
    void create_BlankNickname(String nickname) {
        assertThatThrownBy(() -> User.create(EMAIL, nickname, ENCODED_PASSWORD))
            .isInstanceOf(UserException.class)
            .extracting("errorCode")
            .isEqualTo(UserErrorCode.USER_NICKNAME_INVALID);
    }

}
