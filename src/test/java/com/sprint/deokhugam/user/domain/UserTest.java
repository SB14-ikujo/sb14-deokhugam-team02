package com.sprint.deokhugam.user.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.sprint.deokhugam.user.domain.exception.UserErrorCode;
import com.sprint.deokhugam.user.domain.exception.UserException;

public class UserTest {

    private static final String EMAIL = "user@naver.com";
    private static final String ENCODED_PASSWORD = "password";
    private static final String ORIGINAL_NICKNAME = "닉네임";

    private User createUser() {
        return User.create(EMAIL, ORIGINAL_NICKNAME, ENCODED_PASSWORD);
    }

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

    @ParameterizedTest
    @ValueSource(strings = {"ab", "abcdefghijabcdefghij"})
    @DisplayName("2자 이상 20자 이하의 닉네임으로 변경할 수 있다.")
    void changeNickname_withValidNickname(String newNickname) {
        // given
        User user = createUser();

        // when
        user.changeNickname(newNickname);

        // then
        assertThat(user.getNickname()).isEqualTo(newNickname);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"a", "abcdefghijabcdefghijk", "  ", "\t\t"})
    @DisplayName("잘못된 닉네임으로 변경하면 실패하고 기존 닉네임이 유지된다.")
    void changeNickname_withInvalidNickname(String newNickname) {
        // given
        User user = createUser();

        // when, then
        assertThatThrownBy(() -> user.changeNickname(newNickname))
            .isInstanceOf(UserException.class)
            .extracting("errorCode")
            .isEqualTo(UserErrorCode.USER_NICKNAME_INVALID);
        assertThat(user.getNickname()).isEqualTo(ORIGINAL_NICKNAME);
    }

    @Test
    @DisplayName("사용자를 논리 삭제하면 삭제 상태가 된다.")
    void delete() {
        // given
        User user = createUser();

        // when
        user.delete();

        // then
        assertThat(user.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("논리 삭제된 사용자는 닉네임을 변경할 수 없다.")
    void changeNickname_afterDelete() {
        // given
        User user = createUser();
        user.delete();

        // when, then
        assertThatThrownBy(() -> user.changeNickname("새닉네임"))
            .isInstanceOf(UserException.class)
            .extracting("errorCode")
            .isEqualTo(UserErrorCode.USER_NOT_FOUND);
        assertThat(user.getNickname()).isEqualTo(ORIGINAL_NICKNAME);
    }
}
