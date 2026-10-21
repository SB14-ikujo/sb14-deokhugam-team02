package com.sprint.deokhugam.user.adapter.in.web.mapper;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.sprint.deokhugam.user.adapter.in.web.dto.UserDto;
import com.sprint.deokhugam.user.domain.User;

public class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    @DisplayName("User를 UserDto로 변환한다.")
    void toDto() {
        // given
        User user = User.create("user@naver.com", "닉네임", "encoded-password");

        // when
        UserDto dto = userMapper.toDto(user);

        // then
        assertThat(dto.email()).isEqualTo("user@naver.com");
        assertThat(dto.nickname()).isEqualTo("닉네임");
    }

    @Test
    @DisplayName("UserDto에는 비밀번호 필드가 없다.")
    void userDto_hasNoPassword() {
        assertThat(Arrays.stream(UserDto.class.getRecordComponents()).map(RecordComponent::getName))
            .doesNotContain("password");
    }
}
