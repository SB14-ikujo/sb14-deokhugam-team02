package com.sprint.deokhugam.user.adapter.out.security;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BCryptPasswordHasherTest {

    private static final String RAW_PASSWORD = "abcd123!";

    private final BCryptPasswordHasher passwordHasher = new BCryptPasswordHasher();

    @Test
    @DisplayName("비밀번호를 해시하면 원문과 다른 값이 된다.")
    void hash_differentFromRaw() {
        String hashed = passwordHasher.hash(RAW_PASSWORD);

        assertThat(hashed).isNotEqualTo(RAW_PASSWORD);
    }

    @Test
    @DisplayName("같은 비밀번호도 해시할 떄마다 다른 값이 된다.")
    void hash_differentEachTime() {
        String first = passwordHasher.hash(RAW_PASSWORD);
        String second = passwordHasher.hash(RAW_PASSWORD);

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("원문과 해시값을 비교하면 일치한다.")
    void matches_withCorrectPassword() {
        String hashed = passwordHasher.hash(RAW_PASSWORD);

        assertThat(passwordHasher.matches(RAW_PASSWORD, hashed)).isTrue();
    }

    @Test
    @DisplayName("틀린 비밀번호를 해시값과 비교하면 일치하지 않는다.")
    void matched_withWrongPassword() {
        String hashed = passwordHasher.hash(RAW_PASSWORD);

        assertThat(passwordHasher.matches("Wrong123!", hashed)).isFalse();
    }

}
