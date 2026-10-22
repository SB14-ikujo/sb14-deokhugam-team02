package com.sprint.deokhugam.notification.adapter.in.web.dto;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class NotificationUpdateRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    @DisplayName("confirmed가 null이면 검증에 실패한다.")
    void markAsRead_confirmedIsNull() {
        // given
        NotificationUpdateRequest request = new NotificationUpdateRequest(null);

        // when
        Set<ConstraintViolation<NotificationUpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("알림 상태 정보는 필수값 입니다.");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("confirmed가 true/false면 검증에 성공한다.")
    void markAsRead_success(boolean confirmed) {
        NotificationUpdateRequest request = new NotificationUpdateRequest(confirmed);

        assertThat(validator.validate(request)).isEmpty();
    }

}
