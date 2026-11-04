package com.sprint.deokhugam.user.adapter.in.web.dto;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

public class UserRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("로그인 요청의 이메일이나 비밀번호가 비어 있으면 검증에 실패한다.")
    void loginRequest_blank() {
        assertThat(validator.validate(new UserLoginRequest("", "abcd123!"))).isNotEmpty();
        assertThat(validator.validate(new UserLoginRequest("user@naver.com", " "))).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc123!", "abcdefg!", "12345678!", "abcd1234", "abcd123_", "abcd 123!"})
    @DisplayName("비밀번호 규칙을 지키지 않으면 회원가입 요청 검증에 실패한다.")
    void registerRequest_invalidPassword(String password) {
        UserRegisterRequest request = new UserRegisterRequest("user@naver.com", "닉네임", password);

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    @DisplayName("비밀번호 규칙을 지키면 회원가입 요청 검증에 성공한다.")
    void registerRequest_validPassword() {
        UserRegisterRequest request = new UserRegisterRequest("user@naver.com", "닉네임", "abcd123!");

        assertThat(validator.validate(request)).isEmpty();
    }
}
