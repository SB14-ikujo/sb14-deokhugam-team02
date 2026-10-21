package com.sprint.deokhugam.user.application.port.out;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.sprint.deokhugam.common.config.JpaConfig;
import com.sprint.deokhugam.user.domain.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
class UserRepositoryPortTest {

    @Autowired
    private UserRepositoryPort userRepository;

    @Test
    @DisplayName("이미 가입된 이메일이면 true를 반환한다.")
    void existsByEmail() {
        // given
        userRepository.saveAndFlush(User.create("user@naver.com", "닉네임", "encoded"));

        // when & then
        assertThat(userRepository.existsByEmail("user@naver.com")).isTrue();
        assertThat(userRepository.existsByEmail("other@naver.com")).isFalse();
    }

    @Test
    @DisplayName("논리 삭제된 사용자는 ID로 조회되지 않는다.")
    void findByIdAndIsDeletedFalse() {
        // given
        User activeUser = userRepository.saveAndFlush(User.create("a@naver.com", "활성", "encoded"));
        User deletedUser = User.create("d@naver.com", "삭제", "encoded");
        deletedUser.delete();
        userRepository.saveAndFlush(deletedUser);

        // when & then
        assertThat(userRepository.findByIdAndIsDeletedFalse(activeUser.getId())).isPresent();
        assertThat(userRepository.findByIdAndIsDeletedFalse(deletedUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("논리 삭제된 사용자는 이메일로 조회되지 않는다.")
    void findByEmailAndIsDeletedFalse() {
        // given
        User deletedUser = User.create("d@naver.com", "삭제", "encoded");
        deletedUser.delete();
        userRepository.saveAndFlush(deletedUser);

        // when & then
        assertThat(userRepository.findByEmailAndIsDeletedFalse("d@naver.com")).isEmpty();
    }
}
