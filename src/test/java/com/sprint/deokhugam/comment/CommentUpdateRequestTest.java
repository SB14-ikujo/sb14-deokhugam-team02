package com.sprint.deokhugam.comment;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentUpdateRequest;

class CommentUpdateRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("올바른 요청은 검증을 통과한다")
    void valid() {
        assertThat(validator.validate(new CommentUpdateRequest("수정할 내용"))).isEmpty();
    }

    @Test
    @DisplayName("content가 null이면 검증에 실패한다")
    void contentNull() {
        assertThat(validator.validate(new CommentUpdateRequest(null))).isNotEmpty();
    }

    @Test
    @DisplayName("content가 빈 문자열 또는 공백이면 검증에 실패한다")
    void contentBlank() {
        assertThat(validator.validate(new CommentUpdateRequest(""))).isNotEmpty();
        assertThat(validator.validate(new CommentUpdateRequest("   "))).isNotEmpty();
    }

    @Test
    @DisplayName("content가 500자면 통과하고, 501자면 실패한다")
    void contentLength() {
        assertThat(validator.validate(new CommentUpdateRequest("a".repeat(500)))).isEmpty();
        assertThat(validator.validate(new CommentUpdateRequest("a".repeat(501)))).hasSize(1);
    }
}
