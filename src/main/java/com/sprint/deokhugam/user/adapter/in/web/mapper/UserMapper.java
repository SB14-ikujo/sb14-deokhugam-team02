package com.sprint.deokhugam.user.adapter.in.web.mapper;

import com.sprint.deokhugam.user.adapter.in.web.dto.UserDto;
import com.sprint.deokhugam.user.domain.User;

public interface UserMapper {
    UserDto toDto(User user);
}
