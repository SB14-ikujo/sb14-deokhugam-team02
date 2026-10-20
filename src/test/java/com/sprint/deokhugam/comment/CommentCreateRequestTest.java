package com.sprint.deokhugam.comment;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import com.sprint.deokhugam.comment.adapter.in.web.dto.CommentCreateRequest;

class CommentCreateRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("올바른 요청은 검증을 통과한다")
    void valid() {
        var request = new CommentCreateRequest(UUID.randomUUID(), UUID.randomUUID(), "내용");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("reviewId가 null이면 검증에 실패한다")
    void reviewIdNull() {
        var request = new CommentCreateRequest(null, UUID.randomUUID(), "내용");

        Set<ConstraintViolation<CommentCreateRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("userId가 null이면 검증에 실패한다")
    void userIdNull() {
        var request = new CommentCreateRequest(UUID.randomUUID(), null, "내용");

        assertThat(validator.validate(request)).hasSize(1);
    }

    @Test
    @DisplayName("content가 공백이면 검증에 실패한다")
    void contentBlank() {
        var request = new CommentCreateRequest(UUID.randomUUID(), UUID.randomUUID(), "   ");

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    @DisplayName("content가 500자를 넘으면 검증에 실패한다")
    void contentTooLong() {
        var request = new CommentCreateRequest(UUID.randomUUID(), UUID.randomUUID(), "a".repeat(501));

        assertThat(validator.validate(request)).hasSize(1);
    }

    @Test
    @DisplayName("content가 정확히 500자면 검증을 통과한다")
    void contentMaxLength() {
        var request = new CommentCreateRequest(UUID.randomUUID(), UUID.randomUUID(), "a".repeat(500));

        assertThat(validator.validate(request)).isEmpty();
    }
}
